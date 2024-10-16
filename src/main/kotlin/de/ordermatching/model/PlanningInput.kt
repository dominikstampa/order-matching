package de.ordermatching.model

import de.ordermatching.Configuration
import de.ordermatching.INetworkInfo
import de.ordermatching.distance.DistanceProperty

data class PlanningInput(
    val endNode: Node,
    val allNodes: List<Node>,
    val distanceProperty: DistanceProperty,
    val networkInfo: INetworkInfo,
    val order: Order,
    val config: Configuration
) {

}