package de.ordermatching.planning

import de.ordermatching.INetworkInfo
import de.ordermatching.distance.DistanceProperty
import de.ordermatching.model.GeoPosition
import de.ordermatching.model.Node
import de.ordermatching.model.Order

interface IPlanningInitializer {

    fun getStartNodesInitialized(networkInfo: INetworkInfo, position: GeoPosition): List<Node>

    /*
    should return all nodes of the network
    startNodes could be initialized start nodes and should not be returned again
     */
    fun getAllNodes(networkInfo: INetworkInfo, startNodes: List<Node> = emptyList()): List<Node>

    fun initializeNodes(nodes: List<Node>, order: Order, distanceProperty: DistanceProperty)

}