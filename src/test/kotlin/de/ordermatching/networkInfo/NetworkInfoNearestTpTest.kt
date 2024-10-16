package de.ordermatching.networkInfo

import de.ordermatching.model.GeoPosition
import de.ordermatching.model.TransferPoint
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.system.measureNanoTime
import kotlin.test.Ignore
import kotlin.test.assertEquals

internal class NetworkInfoNearestTpTest : NetworkInfoBaseTest() {

    private val positionKAMarket = GeoPosition(49.00921242608837, 8.403958675734922)

    @BeforeEach
    override fun setUp() {
        super.setUp()
        every { networkInfo.findNearestTransferPoints(any(), any(), any()) } answers { callOriginal() }
    }

    @Test
    fun `test find nearest transfer point`() {
        every { networkInfo.getAllTransferPoints() } returns allTransferPoints

        val tps = networkInfo.findNearestTransferPoints(positionKAMarket, 1, 5000)

        assertEquals(1, tps.size)
        assertEquals(tpCastleKarlsruhe, tps[0])
    }

    @Test
    fun `test find nearest transfer point with tp has same coordinates`() {
        val tp = TransferPoint(GeoPosition(latitude = 14.36097778309052, longitude = 4.200735818652469))
        every { networkInfo.getAllTransferPoints() } returns listOf(tp)
        val tpPosition = tp.position

        val tps = networkInfo.findNearestTransferPoints(tpPosition, 1, 5000)

        assertEquals(1, tps.size)
        assertEquals(tp, tps[0])
    }

    @Test
    fun `test find 3 nearest transfer points`() {
        every { networkInfo.getAllTransferPoints() } returns allTransferPoints

        val tps = networkInfo.findNearestTransferPoints(positionKAMarket, 3, 5000)

        assertEquals(3, tps.size)
        assert(tps.contains(tpCastleKarlsruhe))
        assert(tps.contains(tpWildparkKarlsruhe))
        assert(tps.contains(tpFZIKarlsruhe))
    }

    @Test
    fun `test find 3 nearest transfer points but only 2 available`() {
        every { networkInfo.getAllTransferPoints() } returns allTransferPoints.subList(0, 2)

        val tps = networkInfo.findNearestTransferPoints(positionKAMarket, 3, 5000)

        assertEquals(2, tps.size)
    }

    @Test
    fun `test find transfer points but no transfer points available`() {
        every { networkInfo.getAllTransferPoints() } returns emptyList()

        assert(networkInfo.findNearestTransferPoints(positionKAMarket).isEmpty())
    }

    @Test
    fun `test find transfer points but only one within max distance`() {
        every { networkInfo.getAllTransferPoints() } returns allTransferPoints

        val tps = networkInfo.findNearestTransferPoints(tpFerryConstance.position, 3, 1000)

        assertEquals(1, tps.size)
        assertEquals(tpFerryConstance, tps[0])
    }

    @Ignore
    @Test
    fun `test performance find nearest transfer points`() {
        val allTps =
            (1..100000).map { TransferPoint(GeoPosition(Random.nextDouble(47.0, 54.0), Random.nextDouble(6.0, 15.0))) }
        every { networkInfo.getAllTransferPoints() } returns allTps

        val iterations = 100
        var time = 0L

        for (i in 1..iterations) {
            val randPos = GeoPosition(Random.nextDouble(47.0, 54.0), Random.nextDouble(6.0, 15.0))
            time += measureNanoTime {
                networkInfo.findNearestTransferPoints(randPos, 5, 100000) //choose rather high values to find some tps
            }
        }
        println("Average rounded time in milliseconds: ${(time / iterations) / 1000000}")
        //75ms
    }
}