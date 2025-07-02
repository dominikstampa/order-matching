package de.ordermatching.planning

import de.ordermatching.Configuration
import de.ordermatching.INetworkInfo
import de.ordermatching.distance.PriceProperty
import de.ordermatching.distance.TimeProperty
import de.ordermatching.helper.getEndNode
import de.ordermatching.helper.testLsp
import de.ordermatching.helper.testOrder
import de.ordermatching.model.*
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import kotlin.test.assertEquals

internal open class DefaultPlanningCalculatorTest {

    @MockK
    private lateinit var networkInfo: INetworkInfo

    private val calculator = DefaultPlanningCalculator()
    private val endNode = getEndNode(testOrder)
    private val mockTp = TransferPoint(GeoPosition(50.0, 10.0))
    private val startNode = Node(
        position = mockTp.position,
        transferPoint = mockTp,
        lspOwner = null,
        type = NodeType.NEUTRAL,
        predecessor = null,
        lspToNode = null,
        arrivalTime = testOrder.startTime,
        emissions = 0.0,
        price = 0.0
    )
    private val allNodes = listOf(
        startNode
    )
    private lateinit var input: PlanningInput
    private val serviceInfo = ServiceInfo(
        priceEstimate = 1.0,
        deliveryDateEstimate = testOrder.startTime.plusDays(1),
        emissionsEstimate = 2.0
    )
    private val slowLsp = LogisticsServiceProvider(
        name = "SlowLsp",
        externalInteraction = true,
        deliveryRegion = testLsp.deliveryRegion
    )
    private val slowServiceInfo = ServiceInfo(
        priceEstimate = 1.0,
        deliveryDateEstimate = testOrder.startTime.plusDays(2),
        emissionsEstimate = 2.0
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        input = PlanningInput(endNode, allNodes, TimeProperty(), networkInfo, testOrder, Configuration())
        clearAllMocks()
    }

    @Test
    fun `test calculate basic functionality`() {
        every { networkInfo.findLSPsWithPositionInDeliveryRegion(any()) } returns listOf(testLsp)
        every { networkInfo.findTransferPointsInLSPDeliveryRegion(any()) } returns listOf(mockTp)
        every { networkInfo.findSuitedCWRoutesNearPosition(any(), any()) } returns emptyList()
        every { networkInfo.getServiceInfo(testLsp, any(), testOrder.packageSize, any(), any()) } returns serviceInfo

        calculator.calculate(input)

        assert(endNode.emissions != null)
        assert(endNode.price != null)
        assert(endNode.arrivalTime != null)
    }

    @Test
    fun `test calculate shortest path found`() {
        every { networkInfo.findLSPsWithPositionInDeliveryRegion(any()) } returns listOf(testLsp)
        every { networkInfo.findTransferPointsInLSPDeliveryRegion(any()) } returns listOf(mockTp)
        every { networkInfo.findSuitedCWRoutesNearPosition(any(), any()) } returns emptyList()
        every { networkInfo.getServiceInfo(testLsp, any(), testOrder.packageSize, any(), any()) } returns serviceInfo
        every { networkInfo.getServiceInfo(slowLsp, any(), testOrder.packageSize, any(), any()) } returns slowServiceInfo

        calculator.calculate(input)

        assert(endNode.emissions != null)
        assert(endNode.price != null)
        assert(endNode.arrivalTime != null)
        assertEquals(serviceInfo.deliveryDateEstimate, endNode.arrivalTime)
    }

    @Nested
    inner class OpeningTimesTest {

        private val openingTimes =
            listOf(
                de.ordermatching.model.Timeslot(
                    dayOfWeek = testOrder.startTime.plusDays(1).dayOfWeek,
                    openTime = "10:00",
                    closeTime = "18:00"
                )
            )
        private val tpWithOpeningTimes = TransferPoint(position = GeoPosition(51.0, 11.0), openingTimes = openingTimes)
        private val nodeWithOpeningTimes = Node(
            position = tpWithOpeningTimes.position,
            transferPoint = tpWithOpeningTimes,
            lspOwner = null,
            type = NodeType.NEUTRAL,
            predecessor = null,
            lspToNode = null,
            arrivalTime = null,
            emissions = 0.0,
            price = 0.0
        )
        private val timesAllNodes = listOf(startNode, nodeWithOpeningTimes)
        private lateinit var timesInput: PlanningInput
        private val startLsp = LogisticsServiceProvider(
            name = "StartLsp",
            externalInteraction = true,
            deliveryRegion = GeometryFactory().createPolygon( //must not contain recipient
                arrayOf(
                    Coordinate(0.0, 0.0),
                    Coordinate(10.0, 0.0),
                    Coordinate(10.0, 10.0),
                    Coordinate(0.0, 10.0),
                    Coordinate(0.0, 0.0)
                )
            )
        )
        private val endLsp = LogisticsServiceProvider(
            name = "EndLsp",
            externalInteraction = true,
            deliveryRegion = GeometryFactory().createPolygon(
                arrayOf(
                    Coordinate(0.0, 0.0),
                    Coordinate(60.0, 0.0),
                    Coordinate(60.0, 60.0),
                    Coordinate(0.0, 60.0),
                    Coordinate(0.0, 0.0)
                )
            )
        )
        private val endLspServiceInfo = ServiceInfo(
            priceEstimate = 3.0,
            deliveryDateEstimate = testOrder.startTime.plusDays(2),
            emissionsEstimate = 3.0
        )

        @BeforeEach
        fun innerSetup() {
            timesInput =
                PlanningInput(endNode, timesAllNodes, TimeProperty(), networkInfo, testOrder, Configuration())
        }

        /*
        first transport, second transport has to wait for opening times
         */
        @Test
        fun `test opening times considered`() {
            openingTimeNetworkMocks()

            calculator.calculate(timesInput)

            verify { networkInfo.getServiceInfo(endLsp, testOrder.startTime.plusHours(26), testOrder.packageSize, any(), any()) }
            assert(endNode.arrivalTime != null)
        }

        private fun openingTimeNetworkMocks() {
            every { networkInfo.findLSPsWithPositionInDeliveryRegion(mockTp.position) } returns listOf(startLsp)
            every { networkInfo.findLSPsWithPositionInDeliveryRegion(tpWithOpeningTimes.position) } returns listOf(
                startLsp,
                endLsp
            )
            every { networkInfo.findTransferPointsInLSPDeliveryRegion(startLsp) } returns listOf(
                mockTp,
                tpWithOpeningTimes
            )
            every { networkInfo.findTransferPointsInLSPDeliveryRegion(endLsp) } returns listOf(tpWithOpeningTimes)
            every {
                networkInfo.getServiceInfo(
                    startLsp,
                    startNode.arrivalTime!!,
                    testOrder.packageSize,
                    any(),
                    any()
                )
            } returns serviceInfo
            every { networkInfo.findSuitedCWRoutesNearPosition(any(), any()) } returns emptyList()
            every {
                networkInfo.getServiceInfo(
                    endLsp,
                    serviceInfo.deliveryDateEstimate.plusHours(2),
                    testOrder.packageSize,
                    any(),
                    any()
                )
            } returns endLspServiceInfo
        }
    }

    @Nested
    inner class ExternalInteractionTest {

        private lateinit var externalInput: PlanningInput

        private val cheapLsp = LogisticsServiceProvider(
            name = "cheapLsp",
            externalInteraction = false,
            deliveryRegion = testLsp.deliveryRegion
        )
        private val cheapInfo = ServiceInfo(
            emissionsEstimate = 0.0,
            priceEstimate = 0.0,
            deliveryDateEstimate = testOrder.startTime.plusDays(1)
        )


        @BeforeEach
        fun setUp() {
            externalInput = PlanningInput(endNode, allNodes, PriceProperty(), networkInfo, testOrder, Configuration())
        }

        @Test
        fun `test cheaper lsp is not used because not external interaction possible`() {
            every { networkInfo.findLSPsWithPositionInDeliveryRegion(mockTp.position) } returns listOf(
                testLsp,
                cheapLsp
            )
            every { networkInfo.findTransferPointsInLSPDeliveryRegion(any()) } returns listOf(mockTp)
            every {
                networkInfo.getServiceInfo(
                    testLsp,
                    testOrder.startTime,
                    testOrder.packageSize,
                    any(),
                    any()
                )
            } returns serviceInfo
            every {
                networkInfo.getServiceInfo(
                    cheapLsp,
                    testOrder.startTime,
                    testOrder.packageSize,
                    any(),
                    any()
                )
            } returns cheapInfo
            every { networkInfo.findSuitedCWRoutesNearPosition(any(), any()) } returns emptyList()

            calculator.calculate(externalInput)

            assertEquals(testLsp, endNode.lspToNode)
        }

        @Test
        fun `test no path from neutral start tp`() {
            every { networkInfo.findLSPsWithPositionInDeliveryRegion(mockTp.position) } returns listOf(cheapLsp)
            every { networkInfo.findTransferPointsInLSPDeliveryRegion(any()) } returns listOf(mockTp)
            every {
                networkInfo.getServiceInfo(
                    cheapLsp,
                    testOrder.startTime,
                    testOrder.packageSize,
                    any(),
                    any()
                )
            } returns cheapInfo
            every { networkInfo.findSuitedCWRoutesNearPosition(any(), any()) } returns emptyList()

            calculator.calculate(externalInput)

            assertNull(endNode.arrivalTime)
        }
    }
}