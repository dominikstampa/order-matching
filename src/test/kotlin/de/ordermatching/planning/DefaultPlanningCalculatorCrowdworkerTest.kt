package de.ordermatching.planning

import de.ordermatching.Configuration
import de.ordermatching.INetworkInfo
import de.ordermatching.distance.TimeProperty
import de.ordermatching.helper.testCwRoute
import de.ordermatching.helper.testOrder
import de.ordermatching.model.*
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory

internal class DefaultPlanningCalculatorCrowdworkerTest {

    @MockK
    private lateinit var networkInfo: INetworkInfo

    private lateinit var input: PlanningInput
    private val planner = DefaultPlanningCalculator()

    private val startTp = TransferPoint(testOrder.senderPosition)
    private val midTp = TransferPoint(GeoPosition(50.0, 7.0))

    private val endNode = Node(
        position = testOrder.recipientPosition,
        transferPoint = null,
        lspOwner = null,
        type = NodeType.END,
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
    private val allNodes = listOf(
        startNode, midNode
    )
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
    private val routeTimeslot = CrowdworkerRouteTimeslot(testCwRoute.timeslots[0], testCwRoute)
    private val serviceInfo = ServiceInfo(
        deliveryDateEstimate = testOrder.startTime.plusDays(1),
        priceEstimate = 1.0,
        emissionsEstimate = 2.0
    )


    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        input = PlanningInput(endNode, allNodes, TimeProperty(), networkInfo, testOrder, Configuration())
    }

    @Test
    fun `test calculate with lsp and crowdworker`() {
        every { networkInfo.findLSPsWithPositionInDeliveryRegion(testOrder.senderPosition) } returns emptyList()
        every { networkInfo.findLSPsWithPositionInDeliveryRegion(midTp.position) } returns listOf(lastMileLsp)
        every { networkInfo.findSuitedCWRoutesNearPosition(any(), any()) } returns emptyList()
        every { networkInfo.findSuitedCWRoutesNearPosition(testOrder.senderPosition, any()) } returns listOf(
            routeTimeslot
        )
        every { networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(testCwRoute, any()) } returns listOf(
            midTp
        )
        every { networkInfo.findTransferPointsInLSPDeliveryRegion(lastMileLsp) } returns listOf(midTp)
        every { networkInfo.getServiceInfo(lastMileLsp, any(), any()) } returns serviceInfo

        planner.calculate(input)

        assert(endNode.arrivalTime != null)
        assertEquals(lastMileLsp, endNode.lspToNode)
    }
}