package de.ordermatching.planning

import de.ordermatching.INetworkInfo
import de.ordermatching.helper.testCrowdworker
import de.ordermatching.helper.testLsp
import de.ordermatching.model.*
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals

class DefaultPlanningResultGeneratorTest {

    @MockK
    private lateinit var networkInfo: INetworkInfo

    private val resultGenerator = DefaultPlanningResultGenerator()
    private val mockPosition = GeoPosition(0.0, 0.0)
    private val mockTp = TransferPoint(mockPosition, null)
    private val startTime = OffsetDateTime.of(2023, 11, 21, 8, 0, 0, 0, ZoneOffset.UTC) //Tuesday
    private val endTime = startTime.plusDays(1)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun `test generate resulting path`() {
        val end = createNodeChainAndReturnEndNode()
        val result = resultGenerator.generateResultingPath(end, networkInfo)

        assertEquals(1, result.transportSteps.size)
        val firstStep = result.transportSteps.first()
        assertEquals(startTime, firstStep.startTime)
        assertEquals(endTime, firstStep.endTime)
        assertEquals(testLsp, firstStep.lsp)
        assertEquals(startTime, firstStep.start.arrivalTime)
        assertEquals(endTime, firstStep.end.arrivalTime)
    }

    private fun createNodeChainAndReturnEndNode(): Node {
        val start = Node(
            position = mockPosition,
            transferPoint = mockTp,
            lspOwner = null,
            type = NodeType.NEUTRAL,
            predecessor = null,
            arrivalTime = startTime,
            emissions = 0.0,
            price = 0.0
        )
        return Node(
            position = mockPosition,
            transferPoint = mockTp,
            lspOwner = null,
            type = NodeType.END,
            predecessor = start,
            lspToNode = testLsp,
            arrivalTime = endTime,
            emissions = 3.0,
            price = 2.0
        )
    }

    @Test
    fun `test generate resulting path with non reached end node`() {
        val endNode = Node(
            position = mockPosition,
            transferPoint = mockTp,
            lspOwner = null,
            type = NodeType.NEUTRAL

        )
        val result = resultGenerator.generateResultingPath(endNode, networkInfo)

        assert(result.transportSteps.isEmpty())
    }

    @Test
    fun `test generate resulting path with lsp and crowdworker transports`() {
        every { networkInfo.findCrowdworkerForEdge(any(), any()) } returns listOf(testCrowdworker)
        val end = createNodeChainWithLspAndCwAndReturnEndNode()
        val result = resultGenerator.generateResultingPath(end, networkInfo)

        verify(exactly = 1) { networkInfo.findCrowdworkerForEdge(any(), any()) }
        assertEquals(2, result.transportSteps.size)
        val firstStep = result.transportSteps.first()
        assertEquals(1, firstStep.possibleCrowdworker?.size)
        assertEquals(null, firstStep.lsp)
        val secondStep = result.transportSteps.last()
        assertEquals(null, secondStep.possibleCrowdworker)
        assertEquals(testLsp, secondStep.lsp)
    }

    private fun createNodeChainWithLspAndCwAndReturnEndNode(): Node {
        val start = Node(
            position = mockPosition,
            transferPoint = mockTp,
            lspOwner = null,
            type = NodeType.NEUTRAL,
            predecessor = null,
            arrivalTime = startTime,
            emissions = 0.0,
            price = 0.0
        )
        val mid = Node(
            position = mockPosition,
            transferPoint = mockTp,
            lspOwner = null,
            type = NodeType.NEUTRAL,
            predecessor = start,
            arrivalTime = startTime.plusHours(5),
            lspToNode = null,
            emissions = 0.0,
            price = 1.0
        )
        return Node(
            position = mockPosition,
            transferPoint = mockTp,
            lspOwner = null,
            type = NodeType.END,
            predecessor = mid,
            lspToNode = testLsp,
            arrivalTime = endTime,
            emissions = 3.0,
            price = 2.0
        )
    }
}
