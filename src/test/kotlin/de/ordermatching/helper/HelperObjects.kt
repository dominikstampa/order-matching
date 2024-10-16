package de.ordermatching.helper

import de.ordermatching.model.*
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import java.time.DayOfWeek
import java.time.OffsetDateTime
import java.time.ZoneOffset

val testLsp = LogisticsServiceProvider(
    name = "testLSP",
    externalInteraction = true,
    deliveryRegion = GeometryFactory().createPolygon(
        arrayOf(
            Coordinate(0.0, 0.0),
            Coordinate(60.0, 0.0),
            Coordinate(60.0, 60.0),
            Coordinate(0.0, 60.0),
            Coordinate(0.0, 0.0)
        )
    )
)

val testCwRoute = CrowdworkerRoute(
    timeslots = listOf(de.ordermatching.model.Timeslot(DayOfWeek.MONDAY, openTime = "08:00", closeTime = "09:00")),
    maxDetourMinutes = 10,
    route = GeometryFactory().createLineString(
        arrayOf(
            Coordinate(0.0, 0.0),
            Coordinate(1.0, 2.0),
            Coordinate(3.0, 4.0)
        )
    )
)

val testCrowdworker = Crowdworker(
    name = "Dennis Lotze",
    routes = listOf(testCwRoute)
)

val testOrder = Order(
    senderPosition = GeoPosition(49.02038, 8.41369),
    recipientPosition = GeoPosition(50.93853, 6.98288),
    packageSize = PackageSize.SMALL,
    startTime = OffsetDateTime.of(2023, 11, 21, 8, 0, 0, 0, ZoneOffset.UTC) //Tuesday
)
