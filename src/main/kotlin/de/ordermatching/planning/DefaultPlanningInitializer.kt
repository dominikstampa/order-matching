package de.ordermatching.planning

import de.ordermatching.INetworkInfo
import de.ordermatching.distance.DistanceProperty
import de.ordermatching.model.*
import mu.KotlinLogging

class DefaultPlanningInitializer : IPlanningInitializer {

    private val logger = KotlinLogging.logger {}

    override fun getStartNodesInitialized(networkInfo: INetworkInfo, position: GeoPosition): List<Node> {
        val tps = networkInfo.findNearestTransferPoints(position)
        if (tps.isEmpty()) {
            logger.info { "No start nodes found." }
        }
        return tps.map {
            Node(
                position = it.position,
                lspOwner = it.owner,
                transferPoint = it,
                type = if (it.owner == null) NodeType.NEUTRAL else NodeType.OUT
            )
        }
    }

    private fun getAllNodes(networkInfo: INetworkInfo): List<Node> {
        val tps = networkInfo.getAllTransferPoints()
        val nodes = emptyList<Node>().toMutableList()
        tps.forEach {
            if (it.type != TransferPointType.INTERNAL) {
                addNormalNode(it, nodes)
            }
        }
        return nodes
    }

    /*
    returns all nodes of the network except for the start nodes
     */
    override fun getAllNodes(networkInfo: INetworkInfo, startNodes: List<Node>): List<Node> {
        if (startNodes.isEmpty()) {
            return getAllNodes(networkInfo)
        }
        val tps = networkInfo.getAllTransferPoints()
        val nodes = emptyList<Node>().toMutableList()
        tps.forEach {
            if (it.type != TransferPointType.INTERNAL) { //INTERNAL tps can only be start nodes
                if (!isTpInList(it, startNodes)) {
                    addNormalNode(it, nodes)
                } else {
                    //start node
                    if (it.owner != null) {
                        //only add in node, maybe nothing better?
                        nodes.add(
                            Node(
                                position = it.position,
                                lspOwner = it.owner,
                                transferPoint = it,
                                type = NodeType.IN
                            )
                        )
                    }
                }
            }
        }
        nodes.addAll(startNodes)
        return nodes
    }

    override fun initializeNodes(nodes: List<Node>, order: Order, distanceProperty: DistanceProperty) {
        nodes.forEach { distanceProperty.initDistance(it, order.startTime, it.transferPoint!!.openingTimes) }
    }

    private fun isTpInList(tp: TransferPoint, startNodes: List<Node>): Boolean {
        return startNodes.any { it.position == tp.position && it.lspOwner == tp.owner }
    }

    private fun addNormalNode(tp: TransferPoint, list: MutableList<Node>) {
        if (tp.owner == null) {
            list.add(Node(position = tp.position, lspOwner = null, transferPoint = tp, type = NodeType.NEUTRAL))
        } else {
            list.add(Node(position = tp.position, lspOwner = tp.owner, transferPoint = tp, type = NodeType.IN))
            list.add(Node(position = tp.position, lspOwner = tp.owner, transferPoint = tp, type = NodeType.OUT))
        }
    }

}