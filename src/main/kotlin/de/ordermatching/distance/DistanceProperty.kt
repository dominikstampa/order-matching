package de.ordermatching.distance

import de.ordermatching.model.GeoPosition
import de.ordermatching.model.LogisticsServiceProvider
import de.ordermatching.model.Node
import de.ordermatching.utils.GeoUtils
import de.ordermatching.utils.findNextTimeWithinOpeningTimeslot
import java.time.OffsetDateTime
import java.time.ZoneOffset

const val EMISSION_RATE_DISTANCE_FACTOR =
    2 //currently arbitrary value, should depict the difference between air line distance and actual distance
const val LONG_DISTANCE_THRESHOLD_KM = 80


interface DistanceProperty {

    val comparator: Comparator<Node>
    val ignoreLongDistance: Boolean

    fun initDistance(node: Node, startTime: OffsetDateTime, openingTimes: List<de.ordermatching.model.Timeslot>) {
        if (openingTimes.isEmpty()) {
            node.arrivalTime = startTime.withOffsetSameInstant(ZoneOffset.UTC)
        } else {
            node.arrivalTime = findNextTimeWithinOpeningTimeslot(startTime, openingTimes)
                .withOffsetSameInstant(ZoneOffset.UTC)
        }
        node.emissions = 0.0
        node.price = 0.0
    }

    fun needsUpdate(
        from: Node,
        to: Node,
        arrivalTime: OffsetDateTime,
        emissions: Double,
        price: Double,
        lsp: LogisticsServiceProvider?
    ): Boolean

    //distance object if more properties
    fun updateDistance(
        from: Node,
        to: Node,
        arrivalTime: OffsetDateTime,
        emissions: Double,
        price: Double,
        lsp: LogisticsServiceProvider?
    ) {
        require(arrivalTime.hour != 0 || arrivalTime.minute != 0)
        if (needsUpdate(from, to, arrivalTime, emissions, price, lsp) && !isLongDistanceViolation(from, to)) {
            val estimatedEmissions = estimateEmissions(from.position, to.position, emissions)
            to.arrivalTime = arrivalTime.withOffsetSameInstant(ZoneOffset.UTC)
            to.emissions = from.emissions!! + estimatedEmissions
            to.price = from.price!! + price
            to.predecessor = from
            to.lspToNode = lsp
            updateLongDistanceFlag(from, to)
        }
    }

    /*
    after a long-distance step and a short distance step, no further long-distance steps are allowed
     */
    private fun isLongDistanceViolation(from: Node, to: Node): Boolean {
        if (!ignoreLongDistance && from.hasLongDistanceTransport) {
            //from node must have predecessor
            val fromPoint = from.position
            val toPoint = to.position
            if (GeoUtils.calculateDistanceBetweenGeoPointsInKM(fromPoint, toPoint) > LONG_DISTANCE_THRESHOLD_KM) {
                val predecessorPoint = from.predecessor!!.position
                return GeoUtils.calculateDistanceBetweenGeoPointsInKM(
                    predecessorPoint,
                    fromPoint
                ) <= LONG_DISTANCE_THRESHOLD_KM
            }
        }
        return false
    }

    private fun updateLongDistanceFlag(from: Node, to: Node) {
        if (ignoreLongDistance) {
            return
        }
        to.hasLongDistanceTransport = from.hasLongDistanceTransport
        if (!from.hasLongDistanceTransport) {
            val distance = GeoUtils.calculateDistanceBetweenGeoPointsInKM(from.position, to.position)
            to.hasLongDistanceTransport = distance > LONG_DISTANCE_THRESHOLD_KM
        }
    }

    fun estimateEmissions(from: GeoPosition, to: GeoPosition, emissionRate: Double): Double {
        return GeoUtils.calculateDistanceBetweenGeoPointsInKM(from, to) * EMISSION_RATE_DISTANCE_FACTOR * emissionRate
    }
}