package de.ordermatching.distance

import de.ordermatching.model.Node

interface PriceEstimator {

    fun getPrice(startNode: Node, endNode: Node): Double
}