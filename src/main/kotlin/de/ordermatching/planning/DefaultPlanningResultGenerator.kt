package de.ordermatching.planning

import de.ordermatching.INetworkInfo
import de.ordermatching.model.Edge
import de.ordermatching.model.Node
import de.ordermatching.model.NodeType
import de.ordermatching.model.TransportPlanningResult
import mu.KotlinLogging

class DefaultPlanningResultGenerator : IPlanningResultGenerator {

    private val logger = KotlinLogging.logger {}

    override fun generateResultingPath(endNode: Node, networkInfo: INetworkInfo): TransportPlanningResult {
        if (endNode.predecessor == null) {
            logger.info { "End node has no predecessor. No shortest path has been calculated." }
            return TransportPlanningResult(emptyList())
        }
        require(endNode.type == NodeType.END)
        var current = endNode
        val pathEdges = emptyList<Edge>().toMutableList()
        while (current.predecessor != null) {
            if (current.lspToNode != null) {
                pathEdges.add(0, parcelServiceResultEdge(current))
            } else {
                //do not add an edge when picked up by recipient, i.e. current node is the end node
                if (current.type != NodeType.END) {
                    //cw edge
                    if (current.arrivalTime!!.isBefore(current.predecessor?.arrivalTime) || current.arrivalTime == current.predecessor?.arrivalTime) {
                        logger.error { current.arrivalTime }
                        logger.error { current.predecessor?.arrivalTime }
                        throw IllegalArgumentException("Arrival times of two consecutive nodes must not be the same.")
                    }
                    pathEdges.add(0, crowdworkerResultEdge(current, networkInfo))
                }
            }
            current = current.predecessor!!
        }
        return TransportPlanningResult(pathEdges)
    }

    private fun parcelServiceResultEdge(node: Node): Edge {
        return Edge(
            lsp = node.lspToNode,
            startTime = node.predecessor!!.arrivalTime!!,
            endTime = node.arrivalTime!!,
            start = node.predecessor!!,
            end = node
        )
    }

    private fun crowdworkerResultEdge(node: Node, networkInfo: INetworkInfo): Edge {
        val possibleCw =
            networkInfo.findCrowdworkerForEdge(node.predecessor!!, node)
        require(possibleCw.isNotEmpty()) { "Possible cw was empty from ${node.predecessor!!.position} to ${node.position}" }
        return Edge(
            possibleCrowdworker = possibleCw,
            startTime = node.predecessor!!.arrivalTime!!,
            endTime = node.arrivalTime!!,
            start = node.predecessor!!,
            end = node
        )
    }
}