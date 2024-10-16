package de.ordermatching.networkInfo

import de.ordermatching.helper.getRandomPositionInRect
import de.ordermatching.model.*
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import java.time.DayOfWeek
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

internal object CrowdworkerEntities {

    private val weekdaysMorningTimeslots = listOf(
        de.ordermatching.model.Timeslot(
            dayOfWeek = DayOfWeek.MONDAY,
            openTime = "07:00",
            closeTime = "09:00"
        ),
        de.ordermatching.model.Timeslot(
            dayOfWeek = DayOfWeek.TUESDAY,
            openTime = "07:00",
            closeTime = "09:00"
        ),
        de.ordermatching.model.Timeslot(
            dayOfWeek = DayOfWeek.WEDNESDAY,
            openTime = "07:00",
            closeTime = "09:00"
        ),
        de.ordermatching.model.Timeslot(
            dayOfWeek = DayOfWeek.THURSDAY,
            openTime = "07:00",
            closeTime = "09:00"
        ),
        de.ordermatching.model.Timeslot(
            dayOfWeek = DayOfWeek.FRIDAY,
            openTime = "07:00",
            closeTime = "09:00"
        )
    )

    private val gf = GeometryFactory()

    private val routeKarlsruheBruchsal = de.ordermatching.networkInfo.CrowdworkerEntities.gf.createLineString(
        arrayOf(
            Coordinate(8.425110468071637, 49.011981227814836),
            Coordinate(8.424003089894798, 49.00777725984698),
            Coordinate(8.450364080183656, 49.00290312599595),
            Coordinate(8.559862371453585, 49.148327604558915),
            Coordinate(8.58872, 49.11784)
        )
    )

    private val routeKarlsruheDurlach = de.ordermatching.networkInfo.CrowdworkerEntities.gf.createLineString(
        arrayOf(
            Coordinate(8.368408970603703, 49.005499201316006),
            Coordinate(8.412317202542352, 49.006062761304015),
            Coordinate(8.42443827740704, 49.00326902778403),
            Coordinate(8.426591055071414, 49.000720760584386),
            Coordinate(8.437402373060634, 49.00513833777139),
            Coordinate(8.46466266337867, 49.00009686883238)
        )
    )

    private val routeConstanceLagoFerry = de.ordermatching.networkInfo.CrowdworkerEntities.gf.createLineString(
        arrayOf(
            Coordinate(9.176633508521508, 47.657776498460166),
            Coordinate(9.17819525511586, 47.665791558466836),
            Coordinate(9.210544285834496, 47.68218561036603)
        )
    )

    private val routeDurlachBruchsalPublicTransport =
        de.ordermatching.networkInfo.CrowdworkerEntities.gf.createLineString(
            arrayOf(
                Coordinate(8.4623, 49.0021),
                Coordinate(8.5898, 49.1244)
            )
        )

    val cwRouteKAtoBruchsal = CrowdworkerRoute(
        timeslots = de.ordermatching.networkInfo.CrowdworkerEntities.weekdaysMorningTimeslots,
        maxDetourMinutes = 10,
        route = de.ordermatching.networkInfo.CrowdworkerEntities.routeKarlsruheBruchsal
    )

    val cwRouteKAtoBruchsalWeekend = CrowdworkerRoute(
        timeslots = listOf(
            de.ordermatching.model.Timeslot(
                dayOfWeek = DayOfWeek.SATURDAY,
                openTime = "10:00",
                closeTime = "14:00"
            ),
            de.ordermatching.model.Timeslot(
                dayOfWeek = DayOfWeek.SUNDAY,
                openTime = "15:00",
                closeTime = "19:00"
            )
        ),
        maxDetourMinutes = 5,
        route = de.ordermatching.networkInfo.CrowdworkerEntities.routeKarlsruheBruchsal
    )

    val cwRouteKAtoBruchsalMonday = CrowdworkerRoute(
        timeslots = listOf(
            de.ordermatching.model.Timeslot(
                dayOfWeek = DayOfWeek.MONDAY,
                openTime = "10:00",
                closeTime = "14:00"
            )
        ),
        maxDetourMinutes = 5,
        route = de.ordermatching.networkInfo.CrowdworkerEntities.routeKarlsruheBruchsal
    )

    val cwRouteKAtoDurlachBike = CrowdworkerRoute(
        timeslots = de.ordermatching.networkInfo.CrowdworkerEntities.weekdaysMorningTimeslots,
        maxDetourMinutes = 10,
        route = de.ordermatching.networkInfo.CrowdworkerEntities.routeKarlsruheDurlach,
        meansOfTransport = MeansOfTransport.BIKE
    )

    val cwRouteKAtoDurlachCar = CrowdworkerRoute(
        timeslots = de.ordermatching.networkInfo.CrowdworkerEntities.weekdaysMorningTimeslots,
        maxDetourMinutes = 10,
        route = de.ordermatching.networkInfo.CrowdworkerEntities.routeKarlsruheDurlach,
        meansOfTransport = MeansOfTransport.CAR
    )

    val cwRouteConstance = CrowdworkerRoute(
        timeslots = de.ordermatching.networkInfo.CrowdworkerEntities.weekdaysMorningTimeslots,
        maxDetourMinutes = 10,
        route = de.ordermatching.networkInfo.CrowdworkerEntities.routeConstanceLagoFerry
    )

    val cwRouteDurlachBruchsalPublicTransport = CrowdworkerRoute(
        timeslots = de.ordermatching.networkInfo.CrowdworkerEntities.weekdaysMorningTimeslots,
        maxDetourMinutes = 10,
        route = de.ordermatching.networkInfo.CrowdworkerEntities.routeDurlachBruchsalPublicTransport,
        meansOfTransport = MeansOfTransport.PUBLIC_TRANSPORT
    )

    val commuterKarlsruheBruchsal = Crowdworker(
        name = "Dennis Lotze",
        routes = listOf(de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsal)
    )

    val crowdworkerKarlsruheDurlach = Crowdworker(
        name = "Nico Gyarmati",
        routes = listOf(de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoDurlachBike)
    )

    val crowdworkerKAMixed = Crowdworker(
        name = "Melissa Elliot",
        routes = listOf(
            de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsalWeekend,
            de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoDurlachCar
        )
    )

    val crowdworkerKAMonday = Crowdworker(
        name = "Sabine Winter",
        routes = listOf(de.ordermatching.networkInfo.CrowdworkerEntities.cwRouteKAtoBruchsalMonday)
    )

    fun generateCwsWithRouteInRect(
        amount: Int, detourMinutes: Int = 15, bottomLeft: GeoPosition = GeoPosition(47.0, 6.0),
        topRight: GeoPosition = GeoPosition(54.0, 15.0)
    ): List<Crowdworker> {
        return (1..amount).map {
            Crowdworker(
                name = "abc",
                de.ordermatching.networkInfo.CrowdworkerEntities.generateRandomRoutesInRect(
                    detourMinutes,
                    bottomLeft,
                    topRight
                )
            )
        }
    }

    private fun generateRandomRoutesInRect(
        detourMinutes: Int = 15,
        bottomLeft: GeoPosition,
        topRight: GeoPosition
    ): List<CrowdworkerRoute> {
        val randomAmount = Random.nextInt(1, 4)
        return (1..randomAmount).map {
            CrowdworkerRoute(
                maxDetourMinutes = detourMinutes,
                timeslots = de.ordermatching.networkInfo.CrowdworkerEntities.generateRandomTimeslots(),
                route = de.ordermatching.networkInfo.CrowdworkerEntities.generateRandomLineString(bottomLeft, topRight)
            )
        }
    }

    private fun generateRandomTimeslots(): List<de.ordermatching.model.Timeslot> {
        val randomAmount = Random.nextInt(1, 8)
        return (1..randomAmount).map {
            val startHour = Random.nextInt(1, 21)
            de.ordermatching.model.Timeslot(
                dayOfWeek = DayOfWeek.values().random(),
                openTime = startHour.toString().padStart(2, '0') + ":00",
                closeTime = (startHour + 2).toString().padStart(2, '0') + ":00"
            )
        }
    }

    private fun generateRandomLineString(bottomLeft: GeoPosition, topRight: GeoPosition): LineString {
        val nNodes = Random.nextInt(10, 100)
        val startPos = getRandomPositionInRect(bottomLeft, topRight)
        val startCoord = Coordinate(startPos.longitude, startPos.latitude)
        var direction = 2 * PI * Random.nextDouble()
        val maxDeviation = Math.toRadians(90.0)

        val coords = mutableListOf(startCoord)

        for (i in 1..nNodes) {
            val last = coords.last()
            val deviation = Random.nextDouble(2 * maxDeviation) - maxDeviation
            direction += deviation
            val segmentLength = Random.nextDouble(0.001, 0.01)
            coords.add(Coordinate(last.x + cos(direction) * segmentLength, last.y + sin(direction) * segmentLength))
        }
        return de.ordermatching.networkInfo.CrowdworkerEntities.gf.createLineString(coords.toTypedArray())
    }
}