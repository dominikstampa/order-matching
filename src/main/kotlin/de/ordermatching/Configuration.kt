package de.ordermatching

data class Configuration(
    val startTpAmountLimit: Int = 5,
    val pickupTimeMinutes: Int = 15,
    val crowdworkerPayment: Double = 1.0,
    val pickupDistance: Double = 0.1
) {
}