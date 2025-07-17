package de.ordermatching

import de.ordermatching.distance.DefaultCWPriceEstimator
import de.ordermatching.distance.CWPriceEstimator

data class Configuration(
    val startTpAmountLimit: Int = 5,
    val pickupTimeMinutes: Int = 15,
    val pickupDistance: Double = 0.1,
    val cwPriceEstimator: CWPriceEstimator = DefaultCWPriceEstimator(),
    val mixingAllowed: Boolean = true
) {
}