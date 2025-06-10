package de.ordermatching.distance

import de.ordermatching.model.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class DistancePropertyTest {

    //workaround as mocking the interface directly and calling the default implementation does not work due to a bug in MockK
    //https://github.com/mockk/mockk/issues/64
    abstract class DistancePropertyTestClass : DistanceProperty

    private val distanceProperty = mockk<DistancePropertyTestClass>()

    @MockK
    private lateinit var lsp: LogisticsServiceProvider

    @MockK
    private lateinit var transferPoint: TransferPoint

    private lateinit var timeslot: de.ordermatching.model.Timeslot
    private lateinit var node: Node

    private val startPoint = GeoPosition(49.0, 8.0)
    private val farPoint = GeoPosition(50.0, 8.5)
    private val closePoint = GeoPosition(49.1, 8.1)
    private val emissionRate = 5.0

    private lateinit var nodeFrom: Node
    private lateinit var nodeTo: Node
    private lateinit var nodeClose: Node

    private val fromArrivalTime = OffsetDateTime.of(2022, 11, 15, 15, 0, 0, 0, ZoneOffset.UTC)
    private val arrivalTime = fromArrivalTime?.plusHours(2)


    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        timeslot = de.ordermatching.model.Timeslot(
            openTime = "10:00",
            closeTime = "16:00",
            dayOfWeek = DayOfWeek.TUESDAY
        )
        node = Node(
            position = GeoPosition(10.0, 10.0),
            transferPoint = transferPoint,
            lspOwner = lsp,
            type = NodeType.OUT,
            predecessor = null,
            lspToNode = null,
            arrivalTime = null,
            emissions = null,
            price = null
        )
        initNodes()
        every { distanceProperty.initDistance(any(), any(), any()) } answers { callOriginal() }
        every { distanceProperty.estimateEmissions(any(), any(), any()) } answers { callOriginal() }
        every { distanceProperty.updateDistance(any(), any(), any(), any(), any(), any()) } answers { callOriginal() }
        every { distanceProperty.needsUpdate(any(), any(), any(), any(), any()) } returns true
        every { distanceProperty.ignoreLongDistance } returns false
    }

    @AfterEach
    fun cleanUp() {
        clearMocks(distanceProperty)
    }

    private fun initNodes() {
        nodeFrom = Node(
            position = startPoint,
            transferPoint = transferPoint,
            lspOwner = null,
            type = NodeType.NEUTRAL,
            predecessor = null,
            lspToNode = null,
            arrivalTime = fromArrivalTime,
            emissions = 4.0,
            price = 0.0
        )
        nodeTo = Node(
            position = farPoint,
            transferPoint = transferPoint,
            lspOwner = null,
            type = NodeType.NEUTRAL,
            predecessor = null,
            lspToNode = null,
            arrivalTime = null,
            emissions = null,
            price = null
        )
        nodeClose = Node(
            position = closePoint,
            transferPoint = transferPoint,
            lspOwner = null,
            type = NodeType.NEUTRAL
        )
    }

    @Test
    fun `test init with next timeslot in same week`() {
        val desiredStartTime = OffsetDateTime.of(2023, 4, 24, 18, 0, 0, 0, ZoneOffset.UTC) //Monday
        val expectedStartTime = OffsetDateTime.of(2023, 4, 25, 10, 0, 0, 0, ZoneOffset.UTC)

        distanceProperty.initDistance(node, desiredStartTime, listOf(timeslot))

        assertEquals(0.0, node.price)
        assertEquals(0.0, node.emissions)
        assertEquals(expectedStartTime, node.arrivalTime)
    }

    @Test
    fun `test init with next timeslot in next week`() {
        val desiredStartTime = OffsetDateTime.of(2023, 4, 28, 18, 0, 0, 0, ZoneOffset.UTC) //Friday
        val expectedStartTime = OffsetDateTime.of(2023, 5, 2, 10, 0, 0, 0, ZoneOffset.UTC)

        distanceProperty.initDistance(node, desiredStartTime, listOf(timeslot))

        assertEquals(0.0, node.price)
        assertEquals(0.0, node.emissions)
        assertEquals(expectedStartTime, node.arrivalTime)
    }

    @Test
    fun `test init with start time in timeslot`() {
        val desiredStartTime = OffsetDateTime.of(2023, 4, 25, 12, 0, 0, 0, ZoneOffset.UTC) //Tuesday
        val expectedStartTime = OffsetDateTime.of(2023, 4, 25, 12, 0, 0, 0, ZoneOffset.UTC)

        distanceProperty.initDistance(node, desiredStartTime, listOf(timeslot))

        assertEquals(0.0, node.price)
        assertEquals(0.0, node.emissions)
        assertEquals(expectedStartTime, node.arrivalTime)
    }

    @Test
    fun `test estimate emissions`() {
        val from = GeoPosition(10.0, 50.0)
        val toClose = GeoPosition(11.0, 50.0)
        val toMid = GeoPosition(11.0, 49.0)
        val toFar = GeoPosition(15.0, 55.0)
        val emissionRate = 10.0
        val emissionsClose = distanceProperty.estimateEmissions(from, toClose, emissionRate)
        val emissionsFar = distanceProperty.estimateEmissions(from, toFar, emissionRate)
        val emissionsMid = distanceProperty.estimateEmissions(from, toMid, emissionRate)
        assert(emissionsClose < emissionsMid)
        assert(emissionsClose < emissionsFar)
        assert(emissionsMid < emissionsFar)
    }

    @Test
    fun `test update long distance`() {
        distanceProperty.updateDistance(
            nodeFrom,
            nodeTo,
            arrivalTime!!,
            emissions = emissionRate,
            lsp = null,
            price = 2.0
        )
        assert(nodeTo.hasLongDistanceTransport)
        assertEquals(nodeFrom.price?.plus(2.0), nodeTo.price)
        assertEquals(arrivalTime, nodeTo.arrivalTime)
    }

    @Test
    fun `test no long distance update`() {
        distanceProperty.updateDistance(
            nodeFrom,
            nodeClose,
            arrivalTime!!,
            emissions = emissionRate,
            lsp = null,
            price = 2.0
        )
        assertFalse(nodeTo.hasLongDistanceTransport)
    }

    @Test
    fun `test carry over long distance flag`() {
        nodeFrom.hasLongDistanceTransport = true
        distanceProperty.updateDistance(
            nodeFrom,
            nodeClose,
            arrivalTime!!,
            emissions = emissionRate,
            lsp = null,
            price = 2.0
        )
        assert(nodeClose.hasLongDistanceTransport)
    }

    @Test
    fun `test update with long distance violation`() {
        val predecessor = Node(
            position = GeoPosition(48.9, 7.9),
            transferPoint = transferPoint,
            lspOwner = null,
            type = NodeType.NEUTRAL,
            predecessor = null,
            arrivalTime = OffsetDateTime.of(2022, 11, 14, 15, 0, 0, 0, ZoneOffset.UTC),
            emissions = 3.0,
            price = 0.0,
            hasLongDistanceTransport = true
        )
        nodeFrom.predecessor = predecessor
        nodeFrom.hasLongDistanceTransport = true
        distanceProperty.updateDistance(
            nodeFrom,
            nodeTo,
            arrivalTime!!,
            emissions = emissionRate,
            lsp = lsp,
            price = 2.0
        )
        assertNull(nodeTo.arrivalTime)
        assertNull(nodeTo.price)
        assertNull(nodeTo.emissions)
        assertNull(nodeTo.predecessor)
    }

    @Test
    fun `test ignore long distance`() {
        every { distanceProperty.ignoreLongDistance } returns true
        val predecessor = Node(
            position = GeoPosition(48.9, 7.9),
            transferPoint = transferPoint,
            lspOwner = null,
            type = NodeType.NEUTRAL,
            predecessor = null,
            arrivalTime = OffsetDateTime.of(2022, 11, 14, 15, 0, 0, 0, ZoneOffset.UTC),
            emissions = 3.0,
            price = 0.0,
            hasLongDistanceTransport = true
        )
        nodeFrom.predecessor = predecessor
        nodeFrom.hasLongDistanceTransport = true
        distanceProperty.updateDistance(
            nodeFrom,
            nodeTo,
            arrivalTime!!,
            emissions = emissionRate,
            lsp = lsp,
            price = 2.0
        )
        assertNotNull(nodeTo.arrivalTime)
        assertNotNull(nodeTo.price)
        assertNotNull(nodeTo.emissions)
        assertNotNull(nodeTo.predecessor)
        assertFalse(nodeTo.hasLongDistanceTransport)
    }
}