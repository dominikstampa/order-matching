package de.ordermatching.utils

import de.ordermatching.model.GeoPosition
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import kotlin.test.assertFailsWith

internal class GeoUtilsTest {

    private val gf = GeometryFactory()
    private val lineKaiserstr = gf.createLineString(
        arrayOf(
            Coordinate(8.395137739771641, 49.01001430149278),
            Coordinate(8.409935650057337, 49.00931891629628)
        )
    )

    @Test
    fun `test calculate distance`() {
        val pos1 = GeoPosition(10.0, 10.0)
        val pos2 = GeoPosition(11.0, 11.0)

        assertEquals(155.98, GeoUtils.calculateDistanceBetweenGeoPointsInKM(pos1, pos2), 1.0)
    }

    @Test
    fun `test calculate close distance`() {
        val pos1 = GeoPosition(10.0, 10.0)
        val pos2 = GeoPosition(10.01, 10.01)

        assertEquals(1.56, GeoUtils.calculateDistanceBetweenGeoPointsInKM(pos1, pos2), 0.1)
    }

    @Test
    fun `test calculate distance with negative and positive coordinates`() {
        val pos1 = GeoPosition(10.0, -10.0)
        val pos2 = GeoPosition(-11.0, 11.0)

        assertEquals(3293.89, GeoUtils.calculateDistanceBetweenGeoPointsInKM(pos1, pos2), 10.0)
    }

    @Test
    fun `test calculate distance with large coordinates `() {
        val pos1 = GeoPosition(370.0, 370.0)
        val pos2 = GeoPosition(10.0, 10.0)
        assertEquals(0.0, GeoUtils.calculateDistanceBetweenGeoPointsInKM(pos1, pos2), 1.0)
    }

    @Test
    fun `test calculate distance with floating point inaccuracy and same coordinate`() {
        val pos = GeoPosition(
            latitude = 14.36097778309052,
            longitude = 4.200735818652469
        ) //this coordinate leads to floating point inaccuracy when calculating sin/cos
        assertEquals(0.0, GeoUtils.calculateDistanceBetweenGeoPointsInKM(pos, pos), 1.0)
    }

    @Test
    fun `test calculate approx distance between line string and point close distance`() {
        val pointCastleKA = gf.createPoint(Coordinate(8.404436128175043, 49.01352005394376))
        assertEquals(
            430.0,
            GeoUtils.calculateApproxDistanceBetweenLineStringAndPointInMeters(lineKaiserstr, pointCastleKA),
            10.0
        ) //roughly 430m (measured with online tool). 10m deviation should be no problem
    }

    @Test
    fun `test calculate approx distance between line string and point medium distance`() {
        val pointHeidelberg = gf.createPoint(Coordinate(8.6850, 49.4083))
        assertEquals(
            48000.0,
            GeoUtils.calculateApproxDistanceBetweenLineStringAndPointInMeters(lineKaiserstr, pointHeidelberg),
            1000.0
        )
    }

    @Test
    fun `test calculate approx distance between line string and point far distance`() {
        val pointHelsinki = gf.createPoint(Coordinate(24.895275210804147, 60.154095478570866))
        assertEquals(
            1625000.0,
            GeoUtils.calculateApproxDistanceBetweenLineStringAndPointInMeters(lineKaiserstr, pointHelsinki),
            5000.0
        ) //deviation is higher, but we are not interested in very exact results
    }

    @Test
    fun `test calculate approx distance between line string and point where line endpoint is closest to point`() {
        val pointDurlacherTor = gf.createPoint(Coordinate(8.417773373400765, 49.0092978705784))
        println(GeoUtils.calculateApproxDistanceBetweenLineStringAndPointInMeters(lineKaiserstr, pointDurlacherTor))

        assertEquals(
            570.0,
            GeoUtils.calculateApproxDistanceBetweenLineStringAndPointInMeters(lineKaiserstr, pointDurlacherTor),
            10.0
        )
    }

    @Test
    fun `test calculate approx distance between line string and point with large coordinates`() {
        val line = gf.createLineString(
            arrayOf(
                Coordinate(100.0, 100.0),
                Coordinate(200.0, 200.0)
            )
        )
        val point = gf.createPoint(Coordinate(100.0, 200.0))
        assertFailsWith<IllegalArgumentException> {
            GeoUtils.calculateApproxDistanceBetweenLineStringAndPointInMeters(
                line,
                point
            )
        }
    }

    @Test
    fun `test find nearest point on line string`() {
        val line = gf.createLineString(
            arrayOf(
                Coordinate(8.0, 49.0),
                Coordinate(9.0, 50.0)
            )
        )
        val point = gf.createPoint(Coordinate(9.0, 49.0))
        val coord = GeoUtils.findClosestPointOnLineString(line, point)
        assertEquals(8.5, coord.x)
        assertEquals(49.5, coord.y)
    }

    @Test
    fun `test find nearest point on line string with empty line`() {
        val line = gf.createLineString()
        val point = gf.createPoint(Coordinate(8.0, 49.0000001))
        assertFailsWith<IllegalArgumentException> { GeoUtils.findClosestPointOnLineString(line, point) }
    }

    @Test
    fun `test find nearest point on line string with endpoint is closest point`() {
        val line = gf.createLineString(
            arrayOf(
                Coordinate(8.0, 49.0),
                Coordinate(9.0, 50.0)
            )
        )
        val point = gf.createPoint(Coordinate(10.0, 50.0))
        val coord = GeoUtils.findClosestPointOnLineString(line, point)
        assertEquals(9.0, coord.x)
        assertEquals(50.0, coord.y)
    }

    @Test
    fun `test split line string at point single segment`() {
        val line = gf.createLineString(
            arrayOf(
                Coordinate(0.0, 0.0),
                Coordinate(1.0, 0.0)
            )
        )
        val point = gf.createPoint(Coordinate(0.5, 0.0))
        val split = GeoUtils.splitLineStringAtPoint(line, point)
        assertEquals(2, split.size)
        assertEquals(line.startPoint, split[0].startPoint)
        assertEquals(point, split[0].endPoint)
        assertEquals(point, split[1].startPoint)
        assertEquals(line.endPoint, split[1].endPoint)
    }

    @Test
    fun `test split line string at point two segments`() {
        val line = gf.createLineString(
            arrayOf(
                Coordinate(0.0, 0.0),
                Coordinate(1.0, 0.0),
                Coordinate(1.5, 0.0)
            )
        )
        val point = gf.createPoint(Coordinate(0.5, 0.0))

        val split = GeoUtils.splitLineStringAtPoint(line, point)

        assertEquals(1, split[0].coordinates.size - 1)
        assertEquals(2, split[1].coordinates.size - 1)
        assertEquals(line.startPoint, split[0].startPoint)
        assertEquals(point, split[0].endPoint)
        assertEquals(point, split[1].startPoint)
        assertEquals(line.getPointN(1), split[1].getPointN(1))
        assertEquals(line.endPoint, split[1].endPoint)
    }

    @Test
    fun `test split line string at line point three segments`() {
        val line = gf.createLineString(
            arrayOf(
                Coordinate(0.0, 0.0),
                Coordinate(1.0, 0.0),
                Coordinate(1.5, 0.0),
                Coordinate(2.0, 1.0)
            )
        )
        val point = gf.createPoint(Coordinate(1.0, 0.00001))

        val split = GeoUtils.splitLineStringAtPoint(line, point)

        assertEquals(1, split[0].coordinates.size - 1)
        assertEquals(2, split[1].coordinates.size - 1)
        assertEquals(line.startPoint, split[0].startPoint)
        assertEquals(line.getPointN(1), split[1].startPoint)
        assertEquals(line.getPointN(2), split[1].getPointN(1))
        assertEquals(line.endPoint, split[1].endPoint)
    }

    @Test
    fun `test split line string at point not on line`() {
        val line = gf.createLineString(
            arrayOf(
                Coordinate(0.0, 0.0),
                Coordinate(1.0, 0.0)
            )
        )
        val point = gf.createPoint(Coordinate(1.0, 1.0))

        assertFailsWith<IllegalArgumentException> { GeoUtils.splitLineStringAtPoint(line, point) }
    }

    @Test
    fun `test split line string at point close to end point`() {
        val line = gf.createLineString(
            arrayOf(
                Coordinate(0.0, 0.0),
                Coordinate(1.0, 0.0)
            )
        )
        val point = gf.createPoint(Coordinate(0.9999, 0.0))
        val split = GeoUtils.splitLineStringAtPoint(line, point)
        assert(split[1].isEmpty)
        assertEquals(line, split[0])
    }

    @Test
    fun `test split line string at point close to start point`() {
        val line = gf.createLineString(
            arrayOf(
                Coordinate(0.0, 0.0),
                Coordinate(1.0, 0.0)
            )
        )
        val point = gf.createPoint(Coordinate(0.00001, 0.0))

        val split = GeoUtils.splitLineStringAtPoint(line, point)
        assert(split[0].isEmpty)
        assertEquals(line, split[1])
    }
}