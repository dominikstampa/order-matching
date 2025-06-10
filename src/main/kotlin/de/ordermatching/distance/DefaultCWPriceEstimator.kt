package de.ordermatching.distance

import de.ordermatching.model.CrowdworkerRouteTimeslot
import de.ordermatching.model.Node

class DefaultCWPriceEstimator : CWPriceEstimator {
    override fun getPrice(startNode: Node, endNode: Node, routeTimeslot: CrowdworkerRouteTimeslot): Double {
        return 1.0
    }
}