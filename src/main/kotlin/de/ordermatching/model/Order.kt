package de.ordermatching.model

import java.time.OffsetDateTime

data class Order(
    val senderPosition: GeoPosition,
    val recipientPosition: GeoPosition,
    val packageSize: PackageSize,
    val startTime: OffsetDateTime,
) {

}
