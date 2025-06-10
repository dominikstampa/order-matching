package de.ordermatching.distance

import de.ordermatching.model.GeoPosition
import de.ordermatching.model.Node
import de.ordermatching.model.NodeType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal class PricePropertyTest {

    private val priceProperty = PriceProperty()
    private val arrivalTime = OffsetDateTime.of(2022, 11, 15, 15, 0, 0, 0, ZoneOffset.UTC)

    private val nodeOne = Node(
        position = GeoPosition(10.0, 10.0),
        transferPoint = null,
        lspOwner = null,
        type = NodeType.NEUTRAL,
        predecessor = null,
        arrivalTime = arrivalTime,
        emissions = 0.0,
        price = 0.0
    )
    private val nodeTwo = Node(
        position = GeoPosition(10.0, 10.0),
        transferPoint = null,
        lspOwner = null,
        type = NodeType.NEUTRAL,
        predecessor = null,
        arrivalTime = null,
        emissions = null,
        price = 4.0
    )

    @BeforeEach
    fun setUp() {
        nodeOne.price = 0.0
        nodeTwo.price = 4.0
    }

    @Test
    fun `test needs update`() {
        assert(priceProperty.needsUpdate(nodeOne, nodeTwo, arrivalTime.plusDays(1), 2.0, 2.0))
    }

    @Test
    fun `test needs update with to node null`() {
        nodeTwo.price = null
        assert(priceProperty.needsUpdate(nodeOne, nodeTwo, arrivalTime.plusDays(1), 2.0, 2.0))
    }

    @Test
    fun `test comparator`() {
        val sortedList = listOf(nodeTwo, nodeOne).sortedWith(priceProperty.comparator)

        Assertions.assertEquals(nodeOne, sortedList.first())
        Assertions.assertEquals(nodeTwo, sortedList.last())
    }

    @Test
    fun `test comparator with null node`() {
        nodeOne.price = null
        val sortedList = listOf(nodeTwo, nodeOne).sortedWith(priceProperty.comparator)

        Assertions.assertEquals(nodeTwo, sortedList.first())
        Assertions.assertEquals(nodeOne, sortedList.last())
    }


}