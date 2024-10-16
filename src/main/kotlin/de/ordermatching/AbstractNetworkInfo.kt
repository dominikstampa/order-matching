package de.ordermatching

import de.ordermatching.model.*
import de.ordermatching.utils.GeoUtils
import de.ordermatching.utils.findNextOpeningTimeslotRestricted
import de.ordermatching.utils.getNextNDays
import de.ordermatching.utils.timeslotOverlapsStartAndEndOnSameDay
import mu.KotlinLogging
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import java.time.LocalDateTime
import java.time.OffsetDateTime

const val CACHE_INVALIDATION_TIME_MINUTES = 5L


abstract class AbstractNetworkInfo : INetworkInfo {

    private val tpsInRegionCache: MutableMap<LogisticsServiceProvider, TPsInRegionCacheEntry> =
        emptyMap<LogisticsServiceProvider, TPsInRegionCacheEntry>().toMutableMap()

    private val logger = KotlinLogging.logger {}

    override fun findNearestTransferPoints(
        position: GeoPosition,
        limit: Int,
        maxDistanceMeters: Int
    ): List<TransferPoint> {
        val allTps = getAllTransferPoints()
        val sortedTps =
            allTps.parallelStream().filter {
                GeoUtils.calculateDistanceBetweenGeoPointsInKM(
                    it.position,
                    position
                ) * 1000 <= maxDistanceMeters
            }.toList()
                .sortedBy {
                    GeoUtils.calculateDistanceBetweenGeoPointsInKM(
                        it.position,
                        position
                    )
                } //slow for very high maxDistance values

        return if (sortedTps.size >= limit) {
            sortedTps.subList(0, limit)
        } else {
            sortedTps
        }
    }

    override fun findLSPsWithPositionInDeliveryRegion(position: GeoPosition): List<LogisticsServiceProvider> {
        val allLsps = getAllLogisticsServiceProvider()
        val coordinate = GeometryFactory().createPoint(Coordinate(position.longitude, position.latitude))
        return allLsps.filter { it.deliveryRegion.contains(coordinate) }
    }

    override fun findTransferPointsInLSPDeliveryRegion(lsp: LogisticsServiceProvider): List<TransferPoint> {
        val entry = tpsInRegionCache[lsp]
        if (entry != null && entry.timestamp.isAfter(
                LocalDateTime.now().minusMinutes(CACHE_INVALIDATION_TIME_MINUTES)
            )
        ) {

            return entry.tps
        }
        logger.debug { "No cached results for delivery region of lsp ${lsp.name}" }
        val allTps = getAllTransferPoints()
        val gf = GeometryFactory()
        //parallel streams for performance, order does not matter
        val tpsInRegion = allTps.parallelStream()
            .filter {
                lsp.deliveryRegion.contains(
                    gf.createPoint(
                        Coordinate(
                            it.position.longitude,
                            it.position.latitude
                        )
                    )
                )
            }.toList()
        tpsInRegionCache[lsp] = TPsInRegionCacheEntry(LocalDateTime.now(), tpsInRegion)
        return tpsInRegion
    }

    //can you prefilter with grid or something similar?
    override fun findSuitedCWRoutesNearPosition(
        position: GeoPosition,
        time: OffsetDateTime,
        daysInAdvance: Int,
    ): List<CrowdworkerRouteTimeslot> {
        val possibleDays = getNextNDays(time, daysInAdvance)
        val point = GeometryFactory().createPoint(Coordinate(position.longitude, position.latitude))
        val allCrowdworker = getAllCrowdworker()
        val nearRouteTimeslots = allCrowdworker.map { it.routes }.flatten()
            .filter {
                GeoUtils.calculateApproxDistanceBetweenLineStringAndPointInMeters(
                    it.route,
                    point
                ) < it.maxDetourMinutes * it.meansOfTransport.getDetourFactor()
            }
            .mapNotNull { route ->
                val timeslot =
                    findNextOpeningTimeslotRestricted(time, route.timeslots, BUFFER_MINUTES, MAX_DAYS_IN_FUTURE)
                if (timeslot != null) {
                    CrowdworkerRouteTimeslot(timeslot, route)
                } else {
                    null
                }
            }

        return nearRouteTimeslots.filter { possibleDays.contains(it.timeslot.dayOfWeek) } //simple version: only search on same day
    }

    override fun findTransferPointsNearCWRouteBetweenPackageAndEndpoint(
        route: CrowdworkerRoute,
        pickupPosition: GeoPosition
    ): List<TransferPoint> {
        if (GeoUtils.calculateDistanceBetweenGeoPointsInKM(
                GeoPosition(route.route.endPoint.y, route.route.endPoint.x),
                pickupPosition
            ) < 0.5
        ) {
            return emptyList()
        }
        val allTps = getAllTransferPoints()
        val gf = GeometryFactory()
        val maxDistanceMeters = route.maxDetourMinutes * route.meansOfTransport.getDetourFactor()
        if (route.meansOfTransport == MeansOfTransport.PUBLIC_TRANSPORT) {
            //only endpoint
            val endPos = GeoPosition(route.route.endPoint.y, route.route.endPoint.x)
            return allTps.filter {
                GeoUtils.calculateDistanceBetweenGeoPointsInKM(
                    it.position,
                    endPos
                ) * 1000 < maxDistanceMeters
            }
        }
        val pickupPoint = gf.createPoint(Coordinate(pickupPosition.longitude, pickupPosition.latitude))
        val closestPointOnRoute = GeoUtils.findClosestPointOnLineString(route.route, pickupPoint)
        val remainingRoute = GeoUtils.splitLineStringAtPoint(route.route, gf.createPoint(closestPointOnRoute)).last()
        if (remainingRoute.isEmpty) {
            //might happen when pickup position is closes to end point of route and far away
            return emptyList()
        }
        return allTps.filter {
            GeoUtils.calculateApproxDistanceBetweenLineStringAndPointInMeters(
                remainingRoute,
                gf.createPoint(Coordinate(it.position.longitude, it.position.latitude))
            ) < maxDistanceMeters
        }
    }

    override fun findCrowdworkerForEdge(startNode: Node, endNode: Node): List<Crowdworker> {
        if (startNode.arrivalTime == null || endNode.arrivalTime == null) {
            throw IllegalArgumentException("Nodes must be initialized.")
        }
        val allCrowdworkers = getAllCrowdworker()

        return allCrowdworkers.filter { cw ->
            cw.routes.any {
                GeoUtils.calculateApproxDistanceBetweenLineStringAndPointInMeters(
                    it.route,
                    startNode.position.toPoint()
                ) < it.getMaxDetourMeters() &&
                        GeoUtils.calculateApproxDistanceBetweenLineStringAndPointInMeters(
                            it.route,
                            endNode.position.toPoint()
                        ) < it.getMaxDetourMeters() &&
                        it.timeslots.any { slot ->
                            timeslotOverlapsStartAndEndOnSameDay(
                                slot,
                                startNode.arrivalTime!!,
                                endNode.arrivalTime!!
                            )
                        }
            }
        }
    }
}