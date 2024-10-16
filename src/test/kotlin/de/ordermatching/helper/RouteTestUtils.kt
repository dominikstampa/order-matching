package de.ordermatching.helper

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun generateRandomLineString(): LineString {
    val gf = GeometryFactory()
    val nNodes = Random.nextInt(10, 100)
    val startPos = getRandomPositionGermany()
    val startCoord = Coordinate(startPos.longitude, startPos.latitude)
    var direction = 2 * PI * Random.nextDouble()
    val maxDeviation = Math.toRadians(90.0)

    val coords = mutableListOf(startCoord)

    for (i in 1..nNodes) {
        val last = coords.last()
        val deviation = Random.nextDouble(2 * maxDeviation) - maxDeviation
        direction += deviation
        val segmentLength = Random.nextDouble(0.001, 0.01)
        coords.add(Coordinate(last.x + cos(direction) * segmentLength, last.y + sin(direction) * segmentLength))
    }
    val line = gf.createLineString(coords.toTypedArray())
    assert(!line.isEmpty)
    return gf.createLineString(coords.toTypedArray())
}