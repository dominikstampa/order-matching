package de.ordermatching.utils

import de.ordermatching.mapper.toGeoPosition
import de.ordermatching.model.GeoPosition
import org.geotools.geometry.jts.JTS
import org.geotools.referencing.crs.DefaultGeographicCRS
import org.locationtech.jts.geom.*
import org.locationtech.jts.operation.distance.DistanceOp
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

object GeoUtils {

    private val gf = GeometryFactory()
    private val tolerance = 0.0001
    fun calculateDistanceBetweenGeoPointsInKM(from: GeoPosition, to: GeoPosition): Double {
        if (from == to) {
            return 0.0
            //this should prevent cases where acos gets NaN because of floating point inaccuracy
        }
        var lat1 = from.latitude
        var lon1 = from.longitude
        var lat2 = to.latitude
        var lon2 = to.longitude
        lat1 = Math.toRadians(lat1)
        lon1 = Math.toRadians(lon1)
        lat2 = Math.toRadians(lat2)
        lon2 = Math.toRadians(lon2)

        val dist =
            acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon1 - lon2)) //maybe lookup table for faster calculation?

        val earthRadius = 6371.01 //Kilometers

        return earthRadius * dist
    }

    /*
    not accurate for far distances, but should work well enough
     */
    fun findClosestPointOnLineString(line: LineString, point: Point): Coordinate {
        if (line.isEmpty) {
            throw IllegalArgumentException("line must not be empty. A closest point of an empty line string to a point cannot be calculated.")
        }
        return DistanceOp(line, point, tolerance).nearestPoints()[0]
    }


    fun calculateApproxDistanceBetweenLineStringAndPointInMetersFast(line: LineString, point: Point): Double {
        val closestCoord = findClosestPointOnLineString(line, point)
        return calculateDistanceBetweenGeoPointsInKM(
            closestCoord.toGeoPosition(),
            point.coordinate.toGeoPosition(),
        )
    }

    fun calculateApproxDistanceBetweenLineStringAndPointInMeters(line: LineString, point: Point): Double {
        val closestCoord = findClosestPointOnLineString(line, point)
        return JTS.orthodromicDistance(
            closestCoord,
            point.coordinate,
            DefaultGeographicCRS.WGS84
        ) //seems to be very slow but a bit more accurate
    }

    /*
    assumption the point lies on the linestring or is very close to it
    method is not super accurate as we do not need accuracy to a few meters
     */
    fun splitLineStringAtPoint(line: LineString, point: Point): Array<LineString> {
        if (arePointsClose(point, line.endPoint)) {
            return arrayOf(line, gf.createLineString())
        }
        if (arePointsClose(point, line.startPoint)) {
            return arrayOf(gf.createLineString(), line)
        }
        val segments = getLineSegmentsOfLineString(line)
        val index = indexOfClosestLineSegment(segments, point.coordinate)
        val splitSegments = splitSegmentAtPoint(segments[index], point)
        val firstSplit = splitSegments[0]
        val firstSegments =
            if (firstSplit != null) segments.subList(0, index) + listOf(firstSplit) else segments.subList(
                0,
                index
            )
        val lastSplit = splitSegments[1]
        val lastSegments = if (lastSplit != null) listOf(lastSplit) + segments.subList(
            index + 1,
            segments.size
        ) else segments.subList(index + 1, segments.size)

        val firstLineString = createLineStringFromSegments(firstSegments)
        val lastLineString = createLineStringFromSegments(lastSegments)

        return arrayOf(firstLineString, lastLineString)
    }

    private fun arePointsClose(point1: Point, point2: Point): Boolean {
        return JTS.orthodromicDistance(point1.coordinate, point2.coordinate, DefaultGeographicCRS.WGS84) < 100
    }

    private fun getLineSegmentsOfLineString(line: LineString): List<LineSegment> {
        val coordinates = line.coordinates
        val segments = coordinates.mapIndexed { index, coord ->
            if (index < coordinates.size - 1) {
                LineSegment(
                    coord,
                    coordinates[index + 1]
                )
            } else {
                LineSegment()
            }
        }
        //list has one empty element at the end
        return segments.dropLast(1)
    }

    /*
    expects that target lies on one of the segments or is very close to it
     */
    private fun indexOfClosestLineSegment(
        lineSegments: List<LineSegment>,
        target: Coordinate
    ): Int {
        var indexOfClosestLineSegment = -1
        lineSegments.forEachIndexed { index, lineSegment ->
            if (lineSegment.distance(target) < tolerance) indexOfClosestLineSegment = index
        }
        if (indexOfClosestLineSegment == -1) {
            throw IllegalArgumentException("target coordinate is not close to any line segment")
        } else {
            return indexOfClosestLineSegment
        }
    }

    /*
    expects point to lie on the segment or be very close to it
     */
    private fun splitSegmentAtPoint(segment: LineSegment, point: Point): Array<LineSegment?> {
        if (arePointsClose(point, gf.createPoint(segment.p0))) {
            return arrayOf(null, segment)
        }
        if (arePointsClose(point, gf.createPoint(segment.p1))) {
            return arrayOf(segment, null)
        }
        return arrayOf(LineSegment(segment.p0, point.coordinate), LineSegment(point.coordinate, segment.p1))
    }

    private fun createLineStringFromSegments(segments: List<LineSegment>): LineString {
        if (segments.isEmpty()) {
            gf.createLineString()
        }
        var coordinates = segments.map { it.p0 }
        coordinates += segments.last().p1
        return gf.createLineString(coordinates.toTypedArray())
    }
}