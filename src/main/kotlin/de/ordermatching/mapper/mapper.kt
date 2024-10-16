package de.ordermatching.mapper

import de.ordermatching.model.GeoPosition
import org.locationtech.jts.geom.Coordinate

fun Coordinate.toGeoPosition(): GeoPosition {
    return GeoPosition(latitude = y, longitude = x)
}