package de.ordermatching.networkInfo

import de.ordermatching.helper.testLsp
import de.ordermatching.model.GeoPosition
import de.ordermatching.model.LogisticsServiceProvider
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.system.measureNanoTime
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class NetworkInfoPointInLSPRegionTest : NetworkInfoBaseTest() {


    @BeforeEach
    override fun setUp() {
        super.setUp()
        every { networkInfo.findLSPsWithPositionInDeliveryRegion(any()) } answers { callOriginal() }
    }

    @Test
    fun `test find lsp with position in delivery region`() {
        every { networkInfo.getAllLogisticsServiceProvider() } returns listOf(testLsp)

        val result = networkInfo.findLSPsWithPositionInDeliveryRegion(GeoPosition(10.0, 10.0))

        assertEquals(1, result.size)
        assertEquals(testLsp, result[0])
    }

    @Test
    fun `test find lsp with position in delivery region with position outside`() {
        every { networkInfo.getAllLogisticsServiceProvider() } returns listOf(testLsp)

        val result = networkInfo.findLSPsWithPositionInDeliveryRegion(GeoPosition(90.0, 90.0))

        assert(result.isEmpty())
    }

    @Test
    fun `test find lsp with position in delivery with position inside multiple regions`() {
        every { networkInfo.getAllLogisticsServiceProvider() } returns listOf(testLsp, lspKarlsruhe)

        val result = networkInfo.findLSPsWithPositionInDeliveryRegion(GeoPosition(49.01361338709833, 8.4044016305449))

        assertEquals(2, result.size)
        assert(result.contains(testLsp))
        assert(result.contains(lspKarlsruhe))
    }

    @Test
    fun `test find lsp with position in delivery with position in and outside`() {
        every { networkInfo.getAllLogisticsServiceProvider() } returns listOf(lspKarlsruhe, lspConstance, testLsp)

        val result = networkInfo.findLSPsWithPositionInDeliveryRegion(GeoPosition(49.01361338709833, 8.4044016305449))

        assertEquals(2, result.size)
        assert(result.contains(testLsp))
        assert(result.contains(lspKarlsruhe))
        assertFalse(result.contains(lspConstance))
    }

    @Test
    fun `test find lsp without lsps available`() {
        every { networkInfo.getAllLogisticsServiceProvider() } returns emptyList()

        val result = networkInfo.findLSPsWithPositionInDeliveryRegion(GeoPosition(49.01361338709833, 8.4044016305449))

        assert(result.isEmpty())
    }

    @Ignore
    @Test
    fun `test performance`() {
        val lsps = generateLsps(1000)

        every { networkInfo.getAllLogisticsServiceProvider() } returns lsps

        val iterations = 100
        var time = 0L

        for (i in 1..iterations) {
            val randPos = GeoPosition(Random.nextDouble(47.0, 54.0), Random.nextDouble(6.0, 15.0))
            time += measureNanoTime {
                networkInfo.findLSPsWithPositionInDeliveryRegion(randPos)
            }
        }
        println("Average rounded time in milliseconds: ${(time / iterations) / 1000000}")
    }

    private fun generateLsps(amount: Int): List<LogisticsServiceProvider> {
        val lsps = (1..amount).map {
            LogisticsServiceProvider(
                name = "KarlsruheLSP",
                externalInteraction = true,
                deliveryRegion = generateRandomPolygon()
            )
        }

        return lsps
    }


}