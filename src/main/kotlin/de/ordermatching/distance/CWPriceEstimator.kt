package de.ordermatching.distance

import de.ordermatching.model.CrowdworkerRouteTimeslot
import de.ordermatching.model.Node

interface CWPriceEstimator {

    fun getPrice(startNode: Node, endNode: Node, routeTimeslot: CrowdworkerRouteTimeslot): Double
}