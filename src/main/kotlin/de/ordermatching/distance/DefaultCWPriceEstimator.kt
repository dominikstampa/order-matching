package de.ordermatching.distance

import de.ordermatching.model.Node

class DefaultCWPriceEstimator : PriceEstimator {
    override fun getPrice(startNode: Node, endNode: Node): Double {
        return 1.0
    }
}