package de.ordermatching.planning

import de.ordermatching.INetworkInfo
import de.ordermatching.distance.TimeProperty
import de.ordermatching.helper.testLsp
import de.ordermatching.helper.testOrder
import de.ordermatching.model.*
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory

class DefaultPlanningInitializerTest {

    @MockK
    private lateinit var networkInfo: INetworkInfo

    private val lsp = LogisticsServiceProvider(
        name = "TestLSP", deliveryRegion = GeometryFactory().createPolygon(
            arrayOf(
                Coordinate(0.0, 0.0),
                Coordinate(10.0, 0.0),
                Coordinate(10.0, 10.0),
                Coordinate(0.0, 0.0)
            )
        )
    )

    private val positionZero = GeoPosition(0.0, 0.0)
    private val positionOne = GeoPosition(1.0, 1.0)
    private val planningInitializer = DefaultPlanningInitializer()
    private val transferPointList = listOf(
        TransferPoint(positionZero),
        TransferPoint(positionOne, lsp)
    )
    private val position = GeoPosition(0.0, 0.0)
    private val startNodes = listOf(
        Node(positionZero, transferPointList[0], null, type = NodeType.NEUTRAL),
        Node(positionOne, transferPointList[1], lsp, type = NodeType.OUT)
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun `test get start nodes`() {
        every { networkInfo.findNearestTransferPoints(position) } returns transferPointList
        val result = planningInitializer.getStartNodesInitialized(networkInfo, position)
        assertEquals(2, result.size)
        assert(result.any { it.type == NodeType.NEUTRAL })
        assert(result.any { it.type == NodeType.OUT })
    }

    @Test
    fun `test get start nodes with empty list`() {
        every { networkInfo.findNearestTransferPoints(position) } returns emptyList()
        val result = planningInitializer.getStartNodesInitialized(networkInfo, position)
        assert(result.isEmpty())
    }

    @Test
    fun `test get all nodes`() {
        every { networkInfo.getAllTransferPoints() } returns transferPointList
        val result = planningInitializer.getAllNodes(networkInfo)
        assertEquals(3, result.size)
        assert(result.any { it.type == NodeType.NEUTRAL })
        assert(result.any { it.type == NodeType.OUT })
        assert(result.any { it.type == NodeType.IN })
    }

    @Test
    fun `test get all nodes but start nodes not twice`() {
        every { networkInfo.getAllTransferPoints() } returns transferPointList
        val result = planningInitializer.getAllNodes(networkInfo, startNodes)
        assertEquals(3, result.size)
        assert(result.any { it.type == NodeType.NEUTRAL })
        assert(result.any { it.type == NodeType.OUT })
        assert(result.any { it.type == NodeType.IN })
    }

    @Test
    fun `test get all nodes without internal nodes`() {
        every { networkInfo.getAllTransferPoints() } returns listOf(
            TransferPoint(
                position = positionZero,
                type = TransferPointType.INTERNAL,
                owner = testLsp
            ),
            TransferPoint(
                position = positionOne,
                type = TransferPointType.SHOP,
                owner = testLsp
            )
        )
        val result = planningInitializer.getAllNodes(networkInfo, emptyList())
        assertEquals(2, result.size)
        assert(result.all { it.transferPoint?.type != TransferPointType.INTERNAL })
    }

    @Test
    fun `test get all nodes without internal nodes with nonempty start nodes`() {
        every { networkInfo.getAllTransferPoints() } returns listOf(
            TransferPoint(
                position = positionZero,
                type = TransferPointType.INTERNAL,
                owner = testLsp
            ),
            TransferPoint(
                position = positionOne,
                type = TransferPointType.SHOP,
                owner = testLsp
            )
        )
        val result = planningInitializer.getAllNodes(networkInfo, startNodes)
        assertEquals(4, result.size)
    }

    @Test
    fun `test initialization`() {
        val nodes = listOf(
            Node(positionZero, transferPointList[0], null, type = NodeType.NEUTRAL),
            Node(positionOne, transferPointList[1], lsp, type = NodeType.OUT)
        )
        planningInitializer.initializeNodes(nodes, testOrder, distanceProperty = TimeProperty())

        assert(nodes[0].arrivalTime != null)
        assert(nodes[0].price != null)
        assert(nodes[0].emissions != null)
        assert(nodes[1].arrivalTime != null)
        assert(nodes[1].price != null)
        assert(nodes[1].emissions != null)
    }
}