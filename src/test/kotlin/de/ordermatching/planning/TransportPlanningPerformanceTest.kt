package de.ordermatching.planning

import de.ordermatching.Configuration
import de.ordermatching.distance.EmissionProperty
import de.ordermatching.helper.NetworkInfoMockImplementation
import de.ordermatching.helper.getRandomPositionInRect
import de.ordermatching.model.*
import io.mockk.MockKAnnotations
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.random.Random
import kotlin.system.measureNanoTime
import kotlin.test.Ignore

/*
performance test for default AbstractNetworkInfo implementations
 */
internal class TransportPlanningPerformanceTest {

    private val networkInfo = NetworkInfoMockImplementation()

    private val emissionProperty = EmissionProperty()
    private val config = Configuration()
    private lateinit var planning: TransportPlanning
    private val orderStartTime = OffsetDateTime.of(2023, 11, 16, 8, 0, 0, 0, ZoneOffset.UTC) //Thursday

    private val numberTps = 1000
    private val numberLsps = 100
    private val numberCw = 1000

    private val testRectBottomLeft = GeoPosition(47.0, 6.0)
    private val testRectTopRight = GeoPosition(48.0, 7.0)

    private val allTps =
        (1..numberTps).map { TransferPoint(getRandomPositionInRect(testRectBottomLeft, testRectTopRight)) }

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        planning = TransportPlanning(
            networkInfo = networkInfo,
            distanceProperty = emissionProperty,
            config = config,
            planningInitializer = DefaultPlanningInitializer(),
            planningCalculator = DefaultPlanningCalculator(),
            planningResultGenerator = DefaultPlanningResultGenerator()
        )
        initMocks()
    }

    @Ignore
    @Test
    fun `test performance`() {
        val iterations = 10
        var time = 0L
        var result: TransportPlanningResult

        for (i in 1..iterations) {
            val order = generateOrder()
            time += measureNanoTime {
                result = planning.calculateTransport(order)
            }
            println("$i : ${result.transportSteps}")
        }

        println("Average rounded time in milliseconds: ${(time / iterations) / 1000000}")
        //last average 6.12.23: 7 seconds, iterations 10
        //with cache this gets a bit tricky, for the first iteration the cache is empty and therefore the calculation probably needs much longer than for the other iterations
        //but is it realistic to use the cache from a previous run, or rather only use the cache from the current run?
    }

    private fun initMocks() {
        networkInfo.setAllCws(
            de.ordermatching.networkInfo.CrowdworkerEntities.generateCwsWithRouteInRect(
                amount = numberCw,
                bottomLeft = testRectBottomLeft,
                topRight = testRectTopRight
            )
        )
        networkInfo.setAllTpsMock(allTps)

        initLsps(numberLsps)
    }

    private fun generateOrder(): Order {
        return Order(
            senderPosition = allTps.random().position,
            recipientPosition = getRandomPositionInRect(testRectBottomLeft, testRectTopRight),
            packageSize = PackageSize.SMALL,
            startTime = orderStartTime
        )
    }

    private fun initLsps(amount: Int) {
        val lsps = (1..amount).map {
            LogisticsServiceProvider(
                name = "KarlsruheLSP",
                externalInteraction = true,
                deliveryRegion = generateRandomPolygonInRect(testRectBottomLeft, testRectTopRight)
            )
        }
        networkInfo.setAllLspsMock(lsps)
    }

    private fun generateRandomPolygonInRect(
        bottomLeft: GeoPosition = GeoPosition(47.0, 6.0),
        topRight: GeoPosition = GeoPosition(54.0, 15.0)
    ): Polygon {
        val startEnd = Coordinate(
            Random.nextDouble(bottomLeft.longitude, topRight.longitude),
            Random.nextDouble(bottomLeft.latitude, topRight.latitude)
        )
        return GeometryFactory().createPolygon(
            arrayOf(
                startEnd,
                Coordinate(
                    Random.nextDouble(bottomLeft.longitude, topRight.longitude),
                    Random.nextDouble(bottomLeft.latitude, topRight.latitude)
                ),
                Coordinate(
                    Random.nextDouble(bottomLeft.longitude, topRight.longitude),
                    Random.nextDouble(bottomLeft.latitude, topRight.latitude)
                ),
                startEnd
            )
        )
    }

}