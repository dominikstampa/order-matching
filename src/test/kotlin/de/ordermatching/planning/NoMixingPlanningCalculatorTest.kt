package de.ordermatching.planning

import de.ordermatching.Configuration
import de.ordermatching.INetworkInfo
import de.ordermatching.distance.DefaultCWPriceEstimator
import de.ordermatching.distance.TimeProperty
import de.ordermatching.helper.TestCWPriceEstimator
import de.ordermatching.helper.testCwRoute
import de.ordermatching.helper.testOrder
import de.ordermatching.model.*
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import java.time.DayOfWeek
import kotlin.test.assertNull

internal class NoMixingPlanningCalculatorTest {

    @MockK
    private lateinit var networkInfo: INetworkInfo

    private lateinit var input: PlanningInput

    private val calculator = DefaultPlanningCalculator()
//    private val calculator = NoMixingPlanningCalculator()
    private val startTp = TransferPoint(testOrder.senderPosition)
    private val midTp = TransferPoint(GeoPosition(50.0, 7.0))
    private val endTp = TransferPoint(testOrder.recipientPosition)
    private val lastMileLsp = LogisticsServiceProvider(
        "lastMile",
        externalInteraction = true,
        deliveryRegion = GeometryFactory().createPolygon(
            arrayOf(
                Coordinate(0.0, 50.0),
                Coordinate(7.0, 50.0),
                Coordinate(7.0, 60.0),
                Coordinate(0.0, 60.0),
                Coordinate(0.0, 50.0)
            )
        )
    )
    private val startNode = Node(
        position = testOrder.senderPosition,
        transferPoint = startTp,
        lspOwner = null,
        type = NodeType.NEUTRAL,
        predecessor = null,
        lspToNode = null,
        arrivalTime = testOrder.startTime,
        emissions = 0.0,
        price = 0.0
    )
    private val midNode = Node(
        position = midTp.position,
        transferPoint = midTp,
        lspOwner = null,
        type = NodeType.NEUTRAL
    )
    private val endTpNode = Node(
        position = endTp.position,
        transferPoint = endTp,
        lspOwner = null,
        type = NodeType.NEUTRAL,
    )
    private val endNode = Node(
        position = testOrder.recipientPosition,
        transferPoint = null,
        lspOwner = null,
        type = NodeType.END,
    )
    private val startRouteTimeslot = CrowdworkerRouteTimeslot(testCwRoute.timeslots[0], testCwRoute)
    val midCwRoute = CrowdworkerRoute(
        timeslots = listOf(de.ordermatching.model.Timeslot(DayOfWeek.MONDAY, openTime = "08:00", closeTime = "09:00")),
        maxDetourMinutes = 10,
        route = GeometryFactory().createLineString(
            arrayOf(
                Coordinate(7.0, 50.0),
                Coordinate(7.0, 51.0),
                Coordinate(7.0, 52.0)
            )
        )
    )
    private val midRouteTimeslot = CrowdworkerRouteTimeslot(midCwRoute.timeslots[0], midCwRoute)

    private val serviceInfo = ServiceInfo(
        deliveryDateEstimate = testOrder.startTime.plusHours(5),
        priceEstimate = 1.0,
        emissionsEstimate = 2.0
    )

    private val allNodes = listOf(
        startNode, midNode, endTpNode
    )

    private val configuration = Configuration(cwPriceEstimator = TestCWPriceEstimator(2.0), mixingAllowed = false)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        input = PlanningInput(endNode, allNodes, TimeProperty(), networkInfo, testOrder, configuration)
    }

    @Test
    fun `test calculate with lsp and crowdworker`() {
        every { networkInfo.findLSPsWithPositionInDeliveryRegion(testOrder.senderPosition) } returns emptyList()
        every { networkInfo.findLSPsWithPositionInDeliveryRegion(midTp.position) } returns listOf(lastMileLsp)
        every { networkInfo.findSuitedCWRoutesNearPosition(any(), any()) } returns emptyList()
        every { networkInfo.findSuitedCWRoutesNearPosition(testOrder.senderPosition, any()) } returns listOf(
            startRouteTimeslot
        )
        every { networkInfo.findSuitedCWRoutesNearPosition(midTp.position, any()) } returns listOf(
            midRouteTimeslot
        )
        every { networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(testCwRoute, any()) } returns listOf(
            midTp
        )
        every { networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(midCwRoute, any()) } returns listOf(
            endTp
        )
        every { networkInfo.findTransferPointsInLSPDeliveryRegion(lastMileLsp) } returns listOf(midTp, endTp)
        every { networkInfo.getServiceInfo(lastMileLsp, any(), any(), any(), any()) } returns serviceInfo

        calculator.calculate(input)

        assert(endNode.arrivalTime != null)
        assert(endTpNode.predecessor != null)
        assertNull(endTpNode.lspToNode) //should be a crowdworker
    }
}