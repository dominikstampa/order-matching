package de.ordermatching.networkInfo

import de.ordermatching.helper.generateRandomLineString
import de.ordermatching.helper.getRandomPositionGermany
import de.ordermatching.model.*
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.system.measureNanoTime
import kotlin.test.Ignore
import kotlin.test.assertEquals

class NetworkInfoTpsNearRouteTest : NetworkInfoBaseTest() {

    private val tpWeingarten = TransferPoint(GeoPosition(49.0589, 8.5213))
    private val tpBruchsalStation = TransferPoint(GeoPosition(49.1250, 8.5908))
    private val routeStartKA = GeoPosition(
        de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsal.route.startPoint.y - 0.0001,
        de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsal.route.startPoint.x - 0.0001
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()
        every {
            networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(
                any(),
                any()
            )
        } answers { callOriginal() }
        every { networkInfo.getAllTransferPoints() } returns listOf(tpCampusBruchsal, tpWeingarten, tpCastleKarlsruhe)
    }

    @Test
    fun `test find tps near route with pickup position start of route`() {
        val result = networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(
            de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsal,
            routeStartKA
        )
        assertEquals(3, result.size)
        assert(result.contains(tpCampusBruchsal))
        assert(result.contains(tpWeingarten))
        assert(result.contains(tpCastleKarlsruhe))
    }

    @Test
    fun `test find tps near route with pickup position in middle of route`() {
        val result = networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(
            de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsal,
            tpWeingarten.position
        )
        assertEquals(2, result.size)
        assert(result.contains(tpCampusBruchsal))
        assert(result.contains(tpWeingarten))
    }

    @Test
    fun `test find tps near route with pickup position at end of route`() {
        //this should not happen
        val result = networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(
            de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsal,
            tpCampusBruchsal.position
        )
        assert(result.isEmpty())
    }

    @Test
    fun `test find tps near route with pickup position at end further away`() {
        //this should not happen
        val result = networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(
            de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsal,
            GeoPosition(tpCampusBruchsal.position.latitude + 0.01, tpCampusBruchsal.position.longitude + 0.2)
        )
        assert(result.isEmpty())
    }

    @Test
    fun `test find tps near route with no tps available`() {
        every { networkInfo.getAllTransferPoints() } returns emptyList()
        assert(
            networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(
                de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsal,
                routeStartKA
            ).isEmpty()
        )
    }

    @Test
    fun `test find tps near route with tps only backwards from pickup position`() {
        every { networkInfo.getAllTransferPoints() } returns listOf(tpCastleKarlsruhe)
        //should never happen as there is at least the tp where the package gets picked up
        val result = networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(
            de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsal,
            tpWeingarten.position
        )
        assert(result.isEmpty())
    }

    @Test
    fun `test find tps near route with public transport`() {
        every { networkInfo.getAllTransferPoints() } returns listOf(tpWeingarten, tpBruchsalStation)
        val posDurlach = GeoPosition(49.0025, 8.4622)
        val result = networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(
            de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteDurlachBruchsalPublicTransport,
            posDurlach
        )

        assertEquals(1, result.size)
        assert(result.contains(tpBruchsalStation))
    }

    @Ignore
    @Test
    fun `test performance`() {
        val allTps =
            (1..100000).map { TransferPoint(getRandomPositionGermany()) }
        every { networkInfo.getAllTransferPoints() } returns allTps

        val iterations = 200
        var time = 0L

        for (i in 1..iterations) {
            val routeLine = generateRandomLineString()
            val route = CrowdworkerRoute(
                route = routeLine,
                maxDetourMinutes = Random.nextInt(5, 20),
                timeslots = de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsal.timeslots, //not relevant,
                meansOfTransport = MeansOfTransport.CAR
            )
            val point = routeLine.getPointN(Random.nextInt(0, routeLine.numPoints))
            val geoPos =
                GeoPosition(point.y + Random.nextDouble(-0.001, 0.001), point.x + Random.nextDouble(-0.001, 0.001))
            time += measureNanoTime {
                networkInfo.findTransferPointsNearCWRouteBetweenPackageAndEndpoint(route, geoPos)
            }
        }
        println("Average rounded time in milliseconds: ${(time / iterations) / 1000000}")
        //151
    }
}