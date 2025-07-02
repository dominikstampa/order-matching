package de.ordermatching

import de.ordermatching.model.*
import java.time.OffsetDateTime

const val BUFFER_MINUTES = 5
const val MAX_DAYS_IN_FUTURE = 6

interface INetworkInfo {

    /**
    Should return the nearest transfer points to the given position
     */
    fun findNearestTransferPoints(
        position: GeoPosition,
        limit: Int = 3,
        maxDistanceMeters: Int = 1000
    ): List<TransferPoint>

    /**
     * Should return all transfer points
     */
    fun getAllTransferPoints(): List<TransferPoint>

    /**
     * Should return all LSPs
     */
    fun getAllLogisticsServiceProvider(): List<LogisticsServiceProvider>

    /**
     * Should return all LSPs whose delivery regions contain the given position
     */
    fun findLSPsWithPositionInDeliveryRegion(position: GeoPosition): List<LogisticsServiceProvider>

    /**
     * Should return all transfer points that are in the delivery region of the given LSP
     */
    fun findTransferPointsInLSPDeliveryRegion(lsp: LogisticsServiceProvider): List<TransferPoint>

    /**
     * Should return general service information (estimated delivery time, emissions ...)
     * of the parcel service given the potential start time, start and end position and package size of a transport
     */
    fun getServiceInfo(
        parcelService: LogisticsServiceProvider, startTime: OffsetDateTime, packageSize: PackageSize, startPosition: GeoPosition, endPosition: GeoPosition,
    ): ServiceInfo

    /**
     * Should return all crowdworker routes that are nearby the given position roughly at the given time
     */
    fun findSuitedCWRoutesNearPosition(
        position: GeoPosition,
        time: OffsetDateTime,
        daysInAdvance: Int = 0
    ): List<CrowdworkerRouteTimeslot>

    /**
     * Should return all transfer points that are nearby the given crowdworker route.
     * But no transfer points that are backwards from the potential pickup position
     */
    fun findTransferPointsNearCWRouteBetweenPackageAndEndpoint(
        route: CrowdworkerRoute,
        pickupPosition: GeoPosition
    ): List<TransferPoint>

    /**
     * Should return crowdworkers that could be able to do a transportation from startNode to endNode at the times given by the nodes.
     */
    fun findCrowdworkerForEdge(startNode: Node, endNode: Node): List<Crowdworker>

    /**
     * Should return all registered crowdworkers
     */
    fun getAllCrowdworker(): List<Crowdworker>

}