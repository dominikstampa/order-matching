package de.ordermatching.model

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point

data class GeoPosition(
    val latitude: Double,
    val longitude: Double
) {

    fun toPoint(): Point {
        return GeometryFactory().createPoint(Coordinate(longitude, latitude))
    }
}