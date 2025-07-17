package de.ordermatching.helper

import de.ordermatching.distance.CWPriceEstimator
import de.ordermatching.model.CrowdworkerRouteTimeslot
import de.ordermatching.model.Node

class TestCWPriceEstimator(private val price: Double = 2.0): CWPriceEstimator {
    override fun getPrice(
        startNode: Node,
        endNode: Node,
        routeTimeslot: CrowdworkerRouteTimeslot
    ): Double {
        return price
    }
}