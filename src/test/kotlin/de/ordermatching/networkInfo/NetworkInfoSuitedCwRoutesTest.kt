package de.ordermatching.networkInfo

import de.ordermatching.helper.getRandomPositionGermany
import de.ordermatching.model.GeoPosition
import io.mockk.clearAllMocks
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.random.Random
import kotlin.system.measureNanoTime
import kotlin.test.Ignore
import kotlin.test.assertEquals

class NetworkInfoSuitedCwRoutesTest : NetworkInfoBaseTest() {

    private val time8Tuesday = OffsetDateTime.of(2023, 11, 14, 8, 0, 0, 0, ZoneOffset.UTC)
    private val posKarlsruhe = GeoPosition(49.00904142036939, 8.417322720217847)

    @BeforeEach
    override fun setUp() {
        super.setUp()
        clearAllMocks()
        every { networkInfo.findSuitedCWRoutesNearPosition(any(), any(), any()) } answers { callOriginal() }
        every { networkInfo.getAllCrowdworker() } returns listOf(
            de.ordermatching.networkInfo.CrowdworkerEntities.crowdworkerKAMixed,
            de.ordermatching.networkInfo.CrowdworkerEntities.commuterKarlsruheBruchsal,
            de.ordermatching.networkInfo.CrowdworkerEntities.crowdworkerKarlsruheDurlach,
            de.ordermatching.networkInfo.CrowdworkerEntities.crowdworkerKAMonday
        )
    }

    @Test
    fun `test find cw routes near position`() {
        val result =
            networkInfo.findSuitedCWRoutesNearPosition(posKarlsruhe, time8Tuesday)

        assertEquals(3, result.size)
        val routes = result.map { it.route }
        assert(routes.contains(de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsal))
        assert(routes.contains(de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoDurlachBike))
        assert(routes.contains(de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoDurlachCar))
    }

    @Test
    fun `test find cw routes near position with no route nearby`() {
        val posBerlin = GeoPosition(52.509639267198644, 13.399424507005747)
        val result =
            networkInfo.findSuitedCWRoutesNearPosition(posBerlin, time8Tuesday)

        assert(result.isEmpty())
    }

    @Test
    fun `test find cw routes near position with bike detour too high`() {
        val posEuropahalle = GeoPosition(48.99699, 8.37680)
        val result =
            networkInfo.findSuitedCWRoutesNearPosition(posEuropahalle, time8Tuesday)

        assertEquals(1, result.size)
        val routes = result.map { it.route }
        assert(routes.contains(de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoDurlachCar))
    }

    @Test
    fun `test find cw routes near position with no route for this time on same day`() { //needs to be adjusted when searching several days in advance
        val time15Tuesday = OffsetDateTime.of(2023, 11, 14, 15, 0, 0, 0, ZoneOffset.UTC)
        val result =
            networkInfo.findSuitedCWRoutesNearPosition(posKarlsruhe, time15Tuesday)

        assert(result.isEmpty())
    }

    @Test
    fun `test find cw routes near position 2 days in advance`() {
        val time8Thursday = OffsetDateTime.of(2023, 11, 16, 8, 0, 0, 0, ZoneOffset.UTC)
        val result = networkInfo.findSuitedCWRoutesNearPosition(posKarlsruhe, time8Thursday, 2)

        assertEquals(4, result.size)
        val routes = result.map { it.route }
        assert(routes.contains(de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsalWeekend))
        assert(!routes.contains(de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsalMonday))
    }

    @Test
    fun `test find cw routes near position with arrival time is end time of timeslot should not find timeslots`() {
        val time9Thursday = OffsetDateTime.of(2023, 11, 16, 9, 0, 0, 0, ZoneOffset.UTC)
        val result = networkInfo.findSuitedCWRoutesNearPosition(posKarlsruhe, time9Thursday, 0)

        assertEquals(0, result.size)
    }

    @Test
    fun `test find cw routes near position with route one week ahead should not be found`() {
        every { networkInfo.getAllCrowdworker() } returns listOf(
            de.ordermatching.networkInfo.CrowdworkerEntities.crowdworkerKAMonday
        )
        val time16Monday = OffsetDateTime.of(2023, 11, 27, 16, 0, 0, 0, ZoneOffset.UTC)
        val result = networkInfo.findSuitedCWRoutesNearPosition(posKarlsruhe, time16Monday, 0)

        assertEquals(0, result.size)
    }


    @Ignore
    @Test
    fun `test performance`() {
        val allCrowdworkers = de.ordermatching.networkInfo.CrowdworkerEntities.generateCwsWithRouteInRect(10000)
        every { networkInfo.getAllCrowdworker() } returns allCrowdworkers

        val iterations = 100
        var timeNanos = 0L

        for (i in 1..iterations) {
            val point = getRandomPositionGermany()
            val time = OffsetDateTime.of(2023, 11, Random.nextInt(1, 8), Random.nextInt(1, 20), 0, 0, 0, ZoneOffset.UTC)

            timeNanos += measureNanoTime {
                networkInfo.findSuitedCWRoutesNearPosition(point, time)
            }
        }

        println("Average rounded time in milliseconds: ${(timeNanos / iterations) / 1000000}")
        //62ms
    }
}