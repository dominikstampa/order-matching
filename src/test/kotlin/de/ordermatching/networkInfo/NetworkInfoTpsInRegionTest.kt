package de.ordermatching.networkInfo

import de.ordermatching.helper.NetworkInfoMockImplementation
import de.ordermatching.model.GeoPosition
import de.ordermatching.model.LogisticsServiceProvider
import de.ordermatching.model.TransferPoint
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import kotlin.random.Random
import kotlin.system.measureNanoTime
import kotlin.test.assertEquals

internal class NetworkInfoTpsInRegionTest : NetworkInfoBaseTest() {

    private val networkInfoMock = NetworkInfoMockImplementation()

    private val lspEmptyRegion = LogisticsServiceProvider(
        name = "EmptyLSP",
        externalInteraction = true,
        deliveryRegion = GeometryFactory().createPolygon(
            arrayOf(
                Coordinate(12.6470540475444, 53.48805974889213),
                Coordinate(12.885940855426133, 53.45335276306306),
                Coordinate(12.684866012697276, 53.38069186901969),
                Coordinate(12.6470540475444, 53.48805974889213)
            )
        )
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun `test find tps in delivery region`() {
        networkInfoMock.setAllTpsMock(allTransferPoints)

        val result = networkInfoMock.findTransferPointsInLSPDeliveryRegion(lspKarlsruhe)

        assertEquals(3, result.size)
        assert(result.contains(tpCastleKarlsruhe))
        assert(result.contains(tpWildparkKarlsruhe))
        assert(result.contains(tpFZIKarlsruhe))
    }

    @Test
    fun `test find tps in delivery region but no tps available`() {
        networkInfoMock.setAllTpsMock(emptyList())

        val result = networkInfoMock.findTransferPointsInLSPDeliveryRegion(lspKarlsruhe)

        assert(result.isEmpty())
    }

    @Test
    fun `test find tps in delivery region but no tps in region`() {
        networkInfoMock.setAllTpsMock(allTransferPoints)

        val result = networkInfoMock.findTransferPointsInLSPDeliveryRegion(lspEmptyRegion)

        assert(result.isEmpty())
    }

    @Test
    fun `test find tps in delivery region using the cache`() {
        networkInfoMock.setAllTpsMock(allTransferPoints)

        networkInfoMock.findTransferPointsInLSPDeliveryRegion(lspKarlsruhe)
        networkInfoMock.setAllTpsMock(emptyList())
        val result = networkInfoMock.findTransferPointsInLSPDeliveryRegion(lspKarlsruhe)
        //old result should be cached

        assert(result.isNotEmpty())
    }

    //    @Ignore
    @Test
    fun `test performance`() {
        val allTps =
            (1..100000).map { TransferPoint(GeoPosition(Random.nextDouble(47.0, 54.0), Random.nextDouble(6.0, 15.0))) }
        networkInfoMock.setAllTpsMock(allTps)

        val iterations = 300
        var time = 0L

        for (i in 1..iterations) {
            val lsp = LogisticsServiceProvider(
                name = "KarlsruheLSP",
                externalInteraction = true,
                deliveryRegion = generateRandomPolygon()
            )
            time += measureNanoTime {
                networkInfoMock.findTransferPointsInLSPDeliveryRegion(lsp)
            }
        }
        println("Average rounded time in milliseconds: ${(time / iterations) / 1000000}")
        println("Average rounded time in microseconds: ${(time / iterations) / 1000}")
        //18ms
        //as we create a new lsp for each iteration, the cache is never used

    }
}