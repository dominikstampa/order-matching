package de.ordermatching.planning

import de.ordermatching.INetworkInfo
import de.ordermatching.model.Node
import de.ordermatching.model.TransportPlanningResult

interface IPlanningResultGenerator {

    fun generateResultingPath(endNode: Node, networkInfo: INetworkInfo): TransportPlanningResult
}