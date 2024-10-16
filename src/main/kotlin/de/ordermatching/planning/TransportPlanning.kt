package de.ordermatching.planning

import de.ordermatching.Configuration
import de.ordermatching.INetworkInfo
import de.ordermatching.distance.DistanceProperty
import de.ordermatching.distance.TimeProperty
import de.ordermatching.model.*
import mu.KotlinLogging

class TransportPlanning(
    private val networkInfo: INetworkInfo,
    private val distanceProperty: DistanceProperty = TimeProperty(),
    private val config: Configuration = Configuration(),
    private val planningInitializer: IPlanningInitializer,
    private val planningResultGenerator: IPlanningResultGenerator,
    private val planningCalculator: IPlanningCalculator
) {

    private var startNodes: List<Node> = emptyList()
    private var allNodes =
        emptyList<Node>() //maybe to array? maybe as parameter as several shortestPathCalc objects need the same
    private lateinit var currentOrder: Order
    private lateinit var endNode: Node

    private val logger = KotlinLogging.logger {}


    //currently the start address of the first step has to be a transfer point
    fun calculateTransport(order: Order): TransportPlanningResult {
        initialize(order)
        val input = PlanningInput(endNode, allNodes, distanceProperty, networkInfo, order, config)
        planningCalculator.calculate(input)
        logger.info { "Generating result for order $order." }
        return planningResultGenerator.generateResultingPath(endNode, networkInfo)
    }

    //maybe in init block instead?
    private fun initialize(order: Order) {
        logger.info { "Initializing Transport planning." }
        currentOrder = order
        endNode = Node(position = order.recipientPosition, null, null, NodeType.END)
        startNodes = planningInitializer.getStartNodesInitialized(networkInfo, order.senderPosition)
        planningInitializer.initializeNodes(startNodes, order, distanceProperty)
        allNodes = planningInitializer.getAllNodes(networkInfo, startNodes)
    }

}