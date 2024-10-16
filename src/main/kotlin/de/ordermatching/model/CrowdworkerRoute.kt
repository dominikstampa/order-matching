package de.ordermatching.model

import org.locationtech.jts.geom.LineString

class CrowdworkerRoute(
    val timeslots: List<de.ordermatching.model.Timeslot>,
    val route: LineString,
    val maxDetourMinutes: Int,
    val meansOfTransport: MeansOfTransport = MeansOfTransport.CAR
) {

    fun getMaxDetourMeters(): Int {
        return maxDetourMinutes * meansOfTransport.getDetourFactor()
    }
}