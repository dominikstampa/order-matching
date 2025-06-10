package de.ordermatching.planning

import de.ordermatching.model.*
import de.ordermatching.utils.GeoUtils
import de.ordermatching.utils.findNextTimeWithinOpeningTimeslot
import de.ordermatching.utils.getDateFromStartTimeAndDayOfWeek
import mu.KotlinLogging
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import java.time.OffsetDateTime
import java.util.*

class DefaultPlanningCalculator : IPlanningCalculator {

    private var reachedDestination = false

    private lateinit var input: PlanningInput
    private lateinit var priorityQueue: PriorityQueue<Node>

    private val logger = KotlinLogging.logger { }

    override fun calculate(
        input: PlanningInput
    ) {
        logger.info { "Starting to calculate transport." }
        this.input = input
        priorityQueue = PriorityQueue(input.distanceProperty.comparator)
        priorityQueue.addAll(this.input.allNodes)
        reachedDestination = false
        logger.info { "Priority Queue size: ${priorityQueue.size}." }
        while (priorityQueue.isNotEmpty() && !reachedDestination) {
            processNode(priorityQueue.poll())
        }
        logger.info { "Ended transport calculation." }
    }

    private fun processNode(node: Node) {
        //maybe instead calculate in special node class instead of node types
        if (node.arrivalTime == null) {
            //only not reachable nodes left -> destination no reachable
            logger.info { "Destination could not be reached." }
            reachedDestination = true
            return
        }
        when (node.type) {
            NodeType.END -> reachedDestination = true
            NodeType.IN -> processInNode(node)
            NodeType.OUT -> processOutNode(node)
            NodeType.NEUTRAL -> processNeutralNode(node)
        }
    }

    private fun inPickupDistance(node: Node): Boolean {
        return GeoUtils.calculateDistanceBetweenGeoPointsInKM(
            input.endNode.position,
            node.position
        ) < input.config.pickupDistance
    }

    private fun selfPickup(node: Node) {
        input.distanceProperty.updateDistance(
            node,
            input.endNode,
            node.arrivalTime!!.plusMinutes(input.config.pickupTimeMinutes.toLong()),
            lsp = null,
            emissions = 0.0,
            price = 0.0
        )
    }

    private fun processInNode(node: Node) {
        requireNotNull(node.lspOwner)
        if (inPickupDistance(node)) {
            selfPickup(node)
        } else {
            var parcelServices =
                input.networkInfo.findLSPsWithPositionInDeliveryRegion(node.position)
            parcelServices =
                parcelServices.filter { it != node.lspOwner } //comparison via equal of data class
            parcelServices.map { parcelService ->
                findAndUpdateNodesForParcelService(parcelService, node)
            }
            processPossibleCWEdges(node)
        }
    }

    private fun findAndUpdateNodesForParcelService(lsp: LogisticsServiceProvider, node: Node) {
        val regionalTransferPoints =
            input.networkInfo.findTransferPointsInLSPDeliveryRegion(lsp)
                .filter { it != node.transferPoint }
        val relevantNodes: MutableList<Node> =
            priorityQueue.filter {
                relevantDestinationNode(it, lsp)
                        && isInTpList(it, regionalTransferPoints) //instead check for region?
            }
                .toMutableList()
        if (isEndInDeliveryRegion(lsp.deliveryRegion)) {
            //performance difference to add end node in filter above?
            relevantNodes.add(input.endNode)
        }
        if (relevantNodes.isNotEmpty()) {
            val serviceInfo =
                input.networkInfo.getServiceInfo(lsp, getEarliestPickupTime(node), input.order.packageSize)
            updateNodes(
                relevantNodes,
                lsp,
                serviceInfo,
                node
            ) //do not consider opening times for parcel service, so they can deliver e.g. day before. Change maybe
        }
    }

    private fun getEarliestPickupTime(node: Node): OffsetDateTime {
        require(node.transferPoint != null)
        val openingTimeslots = node.transferPoint.openingTimes
        return findNextTimeWithinOpeningTimeslot(node.arrivalTime!!, openingTimeslots)
    }

    /*
    checks whether a node can be a destination, when the transport is done by a parcel service that does not own the start node
     */
    private fun relevantDestinationNode(
        node: Node,
        parcelService: LogisticsServiceProvider
    ): Boolean {
        if (node.type == NodeType.NEUTRAL || node.type == NodeType.END) {
            //END node (if no tp) won't be part of regional transferPoints
            return true
        }
        if (node.type == NodeType.IN && node.lspOwner == parcelService) {
            return true
        }
        return node.type == NodeType.OUT && node.lspOwner != parcelService
    }

    private fun isInTpList(node: Node, tpsInRegion: List<TransferPoint>): Boolean {
        return tpsInRegion.any { it == node.transferPoint }
    }

    private fun isEndInDeliveryRegion(region: Polygon): Boolean {
        val location = input.order.recipientPosition
        val gf = GeometryFactory()
        val locationPoint = gf.createPoint(Coordinate(location.longitude, location.latitude))
        return region.contains(locationPoint)
    }

    //TODO function in networkInfo?

    private fun updateNodes(
        nodes: List<Node>,
        parcelService: LogisticsServiceProvider,
        lspServiceInfo: ServiceInfo,
        start: Node
    ) {
        nodes.forEach() {
            input.distanceProperty.updateDistance(
                start,
                it,
                lspServiceInfo.deliveryDateEstimate,
                lspServiceInfo.emissionsEstimate,
                lspServiceInfo.priceEstimate,
                parcelService
            )
            //update node in priority queue
            priorityQueue.remove(it)
            priorityQueue.add(it)
        }
    }

    private fun processOutNode(node: Node) {
        requireNotNull(node.lspOwner)
        val parcelService = node.lspOwner

        if (!parcelService.externalInteraction) {
            val serviceInfo =
                input.networkInfo.getServiceInfo(parcelService, getEarliestPickupTime(node), input.order.packageSize)
            //only IN nodes of this parcel service relevant
            val nodes =
                priorityQueue.filter { it.lspOwner == parcelService && it.type == NodeType.IN }
                    .toMutableList()
            if (isEndInDeliveryRegion(parcelService.deliveryRegion)) {
                nodes.add(input.allNodes.first())
            }
            updateNodes(nodes, parcelService, serviceInfo, node)
        } else {
            //like other nodes
            findAndUpdateNodesForParcelService(parcelService, node)
        }
    }

    private fun processNeutralNode(node: Node) {
        if (inPickupDistance(node)) {
            logger.info { "Set self pickup at node $node" }
            selfPickup(node)
        } else {
            val parcelServices =
                input.networkInfo.findLSPsWithPositionInDeliveryRegion(node.position).filter { it.externalInteraction }
            parcelServices.forEach {
                findAndUpdateNodesForParcelService(it, node)
            }
            processPossibleCWEdges(node)
        }
    }

    private fun processPossibleCWEdges(startNode: Node) {
        val cwRoutes =
            input.networkInfo.findSuitedCWRoutesNearPosition(
                startNode.position,
                startNode.arrivalTime!!
            )
        cwRoutes.forEach {
            findAndUpdateNodesForCWRoute(it, startNode)
        }
    }

    private fun findAndUpdateNodesForCWRoute(routeTimeslot: CrowdworkerRouteTimeslot, node: Node) {
        val transferPoints = input.networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(
            routeTimeslot.route,
            node.position
        )
        val relevantNodes =
            input.allNodes.filter {
                (it.type == NodeType.NEUTRAL || it.type == NodeType.OUT)
                        && isInTpList(it, transferPoints)
                        && it.transferPoint != node.transferPoint
            }
        updateNodesFromCwRoutes(relevantNodes, routeTimeslot, node)
    }

    private fun updateNodesFromCwRoutes(
        nodes: List<Node>,
        routeTimeslot: CrowdworkerRouteTimeslot,
        start: Node
    ) {
        nodes.forEach {
            input.distanceProperty.updateDistance(
                start,
                it,
                getDateFromStartTimeAndDayOfWeek(
                    start.arrivalTime!!,
                    routeTimeslot.timeslot.dayOfWeek,
                    routeTimeslot.timeslot.closeTime //only timeslot as parameter
                ),
                0.0, //can cw emissions be seen as 0? or depending on means of transport
                input.config.cwPriceEstimator.getPrice(start, it), //Replace with some estimate depending on route length ...
                null
            )
            //update node in priority queue (probably the best way in kotlin)
            priorityQueue.remove(it) && priorityQueue.add(it)
        }
    }
}