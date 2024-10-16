package de.ordermatching.model

//line of flight meters per minute detour
const val CAR_DETOUR_FACTOR = 300
const val BIKE_DETOUR_FACTOR = 70
const val FOOT_DETOUR_FACTOR = 30


enum class MeansOfTransport {

    CAR,
    BIKE,
    PUBLIC_TRANSPORT,
    FOOT;

    /*
    returns a very rough estimate of meters detour per minute
     */
    fun getDetourFactor(): Int {
        return when (this) {
            CAR -> CAR_DETOUR_FACTOR
            BIKE -> BIKE_DETOUR_FACTOR
            else -> FOOT_DETOUR_FACTOR
        }
    }
}