package de.ordermatching.planning

import de.ordermatching.Configuration
import de.ordermatching.INetworkInfo
import de.ordermatching.distance.EmissionProperty
import de.ordermatching.helper.*
import de.ordermatching.model.*
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class DefaultPlanningCalculatorLongDistanceTest {

    @MockK
    private lateinit var networkInfo: INetworkInfo

    private val calculator = DefaultPlanningCalculator()
    private val distantOrder = Order(
        senderPosition = GeoPosition(40.0, 10.0),
        recipientPosition = GeoPosition(50.0, 10.0),
        packageSize = PackageSize.SMALL,
        startTime = OffsetDateTime.of(2023, 11, 21, 8, 0, 0, 0, ZoneOffset.UTC) //Tuesday
    )
    private val endNode = getEndNode(distantOrder)
    private val startNode = getStartNode(distantOrder)
    private val secondNode = Node(
        position = GeoPosition(45.0, 10.0),
        transferPoint = TransferPoint(GeoPosition(45.0, 10.0)),
        lspOwner = null,
        type = NodeType.NEUTRAL
    )
    private val thirdNode = Node(
        position = GeoPosition(45.1, 10.0),
        transferPoint = TransferPoint(GeoPosition(45.1, 10.0)),
        lspOwner = null,
        type = NodeType.NEUTRAL
    )
    private val allNodes = listOf(startNode, secondNode, thirdNode, endNode)
    private lateinit var input: PlanningInput
    private val allTps = listOf(startNode.transferPoint!!, secondNode.transferPoint!!, thirdNode.transferPoint!!)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        input = PlanningInput(
            endNode,
            allNodes,
            EmissionProperty(ignoreLongDistance = false),
            networkInfo,
            distantOrder,
            Configuration()
        )
    }


    // forbidden route: startNode ->(testLsp) midNode ->(cw) endTp ->(testLsp) endNode
    // should be route: start -> startTp ->(testLSP) -> endNode
    @Test
    fun `test no long distance transportation after short one`() {
        val serviceInfo = ServiceInfo(
            deliveryDateEstimate = distantOrder.startTime.plusDays(1),
            priceEstimate = 2.0,
            emissionsEstimate = 100.0
        )
        val routeTimeslot = CrowdworkerRouteTimeslot(testCwRoute.timeslots[0], testCwRoute)

        every { networkInfo.findLSPsWithPositionInDeliveryRegion(startNode.position) } returns listOf(testLsp)
        every { networkInfo.findLSPsWithPositionInDeliveryRegion(secondNode.position) } returns listOf(testLsp)
        every { networkInfo.findLSPsWithPositionInDeliveryRegion(thirdNode.position) } returns listOf(testLsp) //add another?
        every { networkInfo.findTransferPointsInLSPDeliveryRegion(testLsp) } returns allTps
        every { networkInfo.getServiceInfo(testLsp, any(), distantOrder.packageSize) } returns serviceInfo
        every { networkInfo.findSuitedCWRoutesNearPosition(any(), any()) } returns emptyList()
        every { networkInfo.findSuitedCWRoutesNearPosition(secondNode.position, any()) } returns listOf(routeTimeslot)
        every {
            networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(
                testCwRoute,
                secondNode.position
            )
        } returns listOf(thirdNode.transferPoint!!)

        calculator.calculate(input)
        assertNotNull(endNode.predecessor)
        assertEquals(startNode, endNode.predecessor)
    }
}