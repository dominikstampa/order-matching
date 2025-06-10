package de.ordermatching.distance

import de.ordermatching.model.GeoPosition
import de.ordermatching.model.LogisticsServiceProvider
import de.ordermatching.model.Node
import de.ordermatching.model.NodeType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

internal class EmissionPropertyTest {

    private val lsp = LogisticsServiceProvider(
        name = "TestLSP",
        externalInteraction = true,
        deliveryRegion = GeometryFactory().createPolygon(
            arrayOf(
                Coordinate(0.0, 0.0),
                Coordinate(10.0, 0.0),
                Coordinate(10.0, 10.0),
                Coordinate(0.0, 0.0)
            )
        )
    )

    private val emissionProperty = EmissionProperty()

    private val positionOne = GeoPosition(10.0, 10.0)
    private val positionTwo = GeoPosition(10.5, 10.5)
    private val distance = 77 //approximate distance between the two points above

    private val nodeOne = Node(
        position = positionOne,
        transferPoint = null,
        lspOwner = null,
        type = NodeType.NEUTRAL,
        predecessor = null,
        arrivalTime = OffsetDateTime.of(2022, 11, 15, 15, 0, 0, 0, ZoneOffset.UTC),
        emissions = 4.0,
        price = 0.0
    )
    private val arrivalTime = OffsetDateTime.of(2022, 11, 15, 16, 0, 0, 0, ZoneOffset.UTC)
    private val nodeTwo = Node(
        position = positionTwo,
        transferPoint = null,
        lspOwner = null,
        type = NodeType.NEUTRAL,
        predecessor = null,
        arrivalTime = arrivalTime,
        emissions = 500.0,
        price = null
    )

    @BeforeEach
    fun setUp() {
        nodeTwo.predecessor = null
        nodeTwo.lspToNode = null
        nodeTwo.arrivalTime = OffsetDateTime.of(2022, 11, 15, 16, 0, 0, 0, ZoneOffset.UTC)
        nodeTwo.emissions = 500.0
        nodeTwo.price = null
    }

    @Test
    fun `test comparator`() {
        val sortedList = listOf(nodeTwo, nodeOne).sortedWith(emissionProperty.comparator)

        Assertions.assertEquals(nodeOne, sortedList.first())
        Assertions.assertEquals(nodeTwo, sortedList.last())
    }

    @Test
    fun `test comparator with emissions null`() {
        nodeTwo.emissions = null

        val sortedList = listOf(nodeTwo, nodeOne).sortedWith(emissionProperty.comparator)

        Assertions.assertEquals(nodeOne, sortedList.first())
        Assertions.assertEquals(nodeTwo, sortedList.last())
    }

    @Test
    fun `test update distance`() {
        val newArrivalTime = OffsetDateTime.of(2022, 11, 14, 16, 0, 0, 0, ZoneOffset.UTC)
        val emissionRate = 3.0
        emissionProperty.updateDistance(
            nodeOne,
            nodeTwo,
            newArrivalTime,
            emissions = emissionRate,
            price = 21.0,
            lsp = lsp
        )
        Assertions.assertEquals(nodeOne, nodeTwo.predecessor)
        Assertions.assertEquals(newArrivalTime, nodeTwo.arrivalTime)
        Assertions.assertEquals(lsp, nodeTwo.lspToNode)
        Assertions.assertEquals(21.0, nodeTwo.price)
        Assertions.assertEquals(
            4.0 + distance * emissionRate * EMISSION_RATE_DISTANCE_FACTOR,
            nodeTwo.emissions!!,
            10.0
        )
    }

    @Test
    fun `test update distance higher emissions`() {
        val newArrivalTime = OffsetDateTime.of(2022, 11, 16, 16, 0, 0, 0, ZoneOffset.UTC)
        emissionProperty.updateDistance(
            nodeOne,
            nodeTwo,
            newArrivalTime,
            emissions = 21.0,
            price = 21.0,
            lsp = lsp
        )
        assertNull(nodeTwo.predecessor)
        Assertions.assertEquals(OffsetDateTime.of(2022, 11, 15, 16, 0, 0, 0, ZoneOffset.UTC), nodeTwo.arrivalTime)
        assertNull(nodeTwo.lspToNode)
        assertNull(nodeTwo.price)
        assertEquals(500.0, nodeTwo.emissions)
    }

    @Test
    fun `test needs update`() {
        assert(
            emissionProperty.needsUpdate(
                nodeOne,
                nodeTwo,
                arrivalTime.minusDays(1),
                emissions = 1.0,
                price = 1.0
            )
        )
    }

    @Test
    fun `test does not need update`() {
        assertFalse(
            emissionProperty.needsUpdate(
                nodeOne,
                nodeTwo,
                arrivalTime.plusDays(1),
                emissions = 20.0,
                price = 1.0
            )
        )
    }
}