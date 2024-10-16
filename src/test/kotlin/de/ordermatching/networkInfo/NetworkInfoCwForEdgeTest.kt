package de.ordermatching.networkInfo

import de.ordermatching.helper.getRandomPositionGermany
import de.ordermatching.model.GeoPosition
import de.ordermatching.model.Node
import de.ordermatching.model.NodeType
import de.ordermatching.model.TransferPoint
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.random.Random
import kotlin.system.measureNanoTime
import kotlin.test.Ignore
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class NetworkInfoCwForEdgeTest : NetworkInfoBaseTest() {


    private val nodeFZI = Node(
        position = tpFZIKarlsruhe.position,
        transferPoint = tpFZIKarlsruhe,
        lspOwner = null,
        type = NodeType.NEUTRAL,
        arrivalTime = OffsetDateTime.of(2023, 11, 20, 8, 0, 0, 0, ZoneOffset.UTC), //MONDAY
        price = 2.0,
        emissions = 0.0
    )

    private val nodeCampusBruchsal = Node(
        position = tpCampusBruchsal.position,
        transferPoint = tpCampusBruchsal,
        lspOwner = null,
        type = NodeType.NEUTRAL,
        predecessor = nodeFZI,
        lspToNode = null,
        arrivalTime = OffsetDateTime.of(2023, 11, 20, 12, 0, 0, 0, ZoneOffset.UTC),
        price = 3.0,
        emissions = 0.0
    )

    private val uninitializedNode = Node(
        position = tpCampusBruchsal.position,
        transferPoint = tpCampusBruchsal,
        lspOwner = null,
        type = NodeType.NEUTRAL,
        predecessor = nodeFZI,
        lspToNode = null
    )

    private val allCw = listOf(
        de.ordermatching.networkInfo.CrowdworkerEntities.commuterKarlsruheBruchsal,
        de.ordermatching.networkInfo.CrowdworkerEntities.crowdworkerKAMonday,
        de.ordermatching.networkInfo.CrowdworkerEntities.crowdworkerKarlsruheDurlach
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()
        every { networkInfo.findCrowdworkerForEdge(any(), any()) } answers { callOriginal() }
    }


    @Test
    fun `test find cw for edge with no cw available`() {
        //should never happen, as we only want to look for cws for an edge when we already found one before
        every { networkInfo.getAllCrowdworker() } returns emptyList()

        assert(networkInfo.findCrowdworkerForEdge(nodeFZI, nodeCampusBruchsal).isEmpty())
    }

    @Test
    fun `test find cw for edge`() {
        every { networkInfo.getAllCrowdworker() } returns allCw

        val result = networkInfo.findCrowdworkerForEdge(nodeFZI, nodeCampusBruchsal)
        assertEquals(2, result.size)
        assertContains(result, de.ordermatching.networkInfo.CrowdworkerEntities.commuterKarlsruheBruchsal)
        assertContains(result, de.ordermatching.networkInfo.CrowdworkerEntities.crowdworkerKAMonday)
    }

    @Test
    fun `test find cw for edge without cw route nearby`() {
        every { networkInfo.getAllCrowdworker() } returns listOf(de.ordermatching.networkInfo.CrowdworkerEntities.crowdworkerKarlsruheDurlach)

        val result = networkInfo.findCrowdworkerForEdge(nodeFZI, nodeCampusBruchsal)
        assert(result.isEmpty())
    }


    @Test
    fun `test find cw for edge without cw route for requested timeslot`() {
        every { networkInfo.getAllCrowdworker() } returns listOf(de.ordermatching.networkInfo.CrowdworkerEntities.crowdworkerKAMixed)

        val result = networkInfo.findCrowdworkerForEdge(nodeFZI, nodeCampusBruchsal)
        assert(result.isEmpty())
    }

    @Test
    fun `test find cw for edge without initialized arrival times`() {
        assertFailsWith<IllegalArgumentException> { networkInfo.findCrowdworkerForEdge(nodeFZI, uninitializedNode) }
    }

    @Ignore
    @Test
    fun `test performance`() {
        val allCrowdworkers = de.ordermatching.networkInfo.CrowdworkerEntities.generateCwsWithRouteInRect(10000, 100)
        every { networkInfo.getAllCrowdworker() } returns allCrowdworkers

        val iterations = 100
        var timeNanos = 0L

        for (i in 1..iterations) {
            val startNode = createStartNode()
            val endNode = createEndNode(startNode)

            timeNanos += measureNanoTime {
                val result = networkInfo.findCrowdworkerForEdge(startNode, endNode)
                println(result.size)
            }
        }

        println("Average rounded time in milliseconds: ${(timeNanos / iterations) / 1000000}")
    }

    private fun getRandomPositionNearPoint(point: GeoPosition): GeoPosition {
        return GeoPosition(
            point.latitude + Random.nextDouble(-1.0, 1.0),
            point.longitude + Random.nextDouble(-1.0, 1.0)
        )
    }

    private fun createStartNode(): Node {
        val position = getRandomPositionGermany()
        return Node(
            position = position,
            transferPoint = TransferPoint(position),
            lspOwner = null,
            type = NodeType.NEUTRAL,
            arrivalTime = OffsetDateTime.of(
                2023,
                11,
                Random.nextInt(1, 8),
                Random.nextInt(1, 18),
                0,
                0,
                0,
                ZoneOffset.UTC
            )
        )
    }

    private fun createEndNode(startNode: Node): Node {
        val position = getRandomPositionNearPoint(startNode.position)
        return Node(
            position = position,
            transferPoint = TransferPoint(position),
            lspOwner = null,
            type = NodeType.NEUTRAL,
            arrivalTime = startNode.arrivalTime!!.plusHours(Random.nextLong(1, 5))
        )
    }


}