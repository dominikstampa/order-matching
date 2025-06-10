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

internal class TimePropertyTest {

    private val timeProperty = TimeProperty()
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
    private val nodeOne = Node(
        position = GeoPosition(10.0, 10.0),
        transferPoint = null,
        lspOwner = null,
        type = NodeType.NEUTRAL,
        predecessor = null,
        arrivalTime = OffsetDateTime.of(2022, 11, 15, 15, 0, 0, 0, ZoneOffset.UTC),
        emissions = 0.0,
        price = 0.0
    )
    private val arrivalTime = OffsetDateTime.of(2022, 11, 15, 16, 0, 0, 0, ZoneOffset.UTC)

    private lateinit var nodeTwo: Node

    @BeforeEach
    fun setUp() {
        nodeTwo = Node(
            position = GeoPosition(10.0, 10.0),
            transferPoint = null,
            lspOwner = null,
            type = NodeType.NEUTRAL,
            predecessor = null,
            arrivalTime = arrivalTime,
            emissions = null,
            price = null
        )
    }

    @Test
    fun `test comparator`() {
        val sortedList = listOf(nodeTwo, nodeOne).sortedWith(timeProperty.comparator)

        assertEquals(nodeOne, sortedList.first())
        assertEquals(nodeTwo, sortedList.last())
    }

    @Test
    fun `test comparator with arrival time null`() {
        nodeTwo.arrivalTime = null

        val sortedList = listOf(nodeTwo, nodeOne).sortedWith(timeProperty.comparator)

        assertEquals(nodeOne, sortedList.first())
        assertEquals(nodeTwo, sortedList.last())
    }

    @Test
    fun `test comparator with arrival time null other way`() {
        nodeOne.arrivalTime = null

        val sortedList = listOf(nodeTwo, nodeOne).sortedWith(timeProperty.comparator)

        assertEquals(nodeTwo, sortedList.first())
        assertEquals(nodeOne, sortedList.last())
    }

    @Test
    fun `test update distance`() {
        val newArrivalTime = OffsetDateTime.of(2022, 11, 14, 16, 0, 0, 0, ZoneOffset.UTC)
        timeProperty.updateDistance(nodeOne, nodeTwo, newArrivalTime, emissions = 0.0, price = 21.0, lsp)
        assertEquals(nodeOne, nodeTwo.predecessor)
        Assertions.assertEquals(newArrivalTime, nodeTwo.arrivalTime)
        Assertions.assertEquals(lsp, nodeTwo.lspToNode)
        Assertions.assertEquals(21.0, nodeTwo.price)
        assertEquals(nodeOne.emissions, nodeTwo.emissions)
    }

    @Test
    fun `test update distance later time`() {
        val newArrivalTime = OffsetDateTime.of(2022, 11, 16, 16, 0, 0, 0, ZoneOffset.UTC)
        timeProperty.updateDistance(nodeOne, nodeTwo, newArrivalTime, emissions = 21.0, price = 21.0, lsp = lsp)
        assertNull(nodeTwo.predecessor)
        Assertions.assertEquals(OffsetDateTime.of(2022, 11, 15, 16, 0, 0, 0, ZoneOffset.UTC), nodeTwo.arrivalTime)
        assertNull(nodeTwo.lspToNode)
        assertNull(nodeTwo.price)
        assertNull(nodeTwo.emissions)
    }

    @Test
    fun `test needs update`() {
        assert(
            timeProperty.needsUpdate(
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
            timeProperty.needsUpdate(
                nodeOne,
                nodeTwo,
                arrivalTime.plusDays(1),
                emissions = 1.0,
                price = 1.0
            )
        )
    }
}