package de.ordermatching.model

import de.ordermatching.distance.DefaultCWPriceEstimator
import de.ordermatching.distance.PriceEstimator

class Crowdworker(
    val name: String,
    val routes: List<CrowdworkerRoute>,
) {
}