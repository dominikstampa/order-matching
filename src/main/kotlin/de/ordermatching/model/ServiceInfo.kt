package de.ordermatching.model

import java.time.OffsetDateTime

data class ServiceInfo(
    val priceEstimate: Double,
    val deliveryDateEstimate: OffsetDateTime,
    val emissionsEstimate: Double
) {

}