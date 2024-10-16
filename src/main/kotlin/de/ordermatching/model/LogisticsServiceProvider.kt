package de.ordermatching.model

import org.locationtech.jts.geom.Polygon

data class LogisticsServiceProvider(
    val name: String? = null,
    val externalInteraction: Boolean = true,
    val deliveryRegion: Polygon
) {

}