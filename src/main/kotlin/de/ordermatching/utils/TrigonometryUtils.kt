package de.ordermatching.utils

import kotlin.math.acos
import kotlin.math.sin

object TrigonometryUtils {

    private const val PRECISION = 100
    private const val MODULUS = 360 * PRECISION

    private const val ACOS_PRECISION = 10000
    private const val ACOS_OFFSET = ACOS_PRECISION

    private val sinTable = (0..360 * PRECISION).map { sin(Math.toRadians(it.toDouble() / PRECISION)) }.toDoubleArray()

    private val acosTable =
        (-1 * ACOS_PRECISION..1 * ACOS_PRECISION).map { acos(it.toDouble() / ACOS_PRECISION) }
            .toDoubleArray()

    fun sinLookup(degrees: Double): Double {
        //no rounding
        return sinLookup((degrees * PRECISION + 0.5).toInt())
    }

    private fun sinLookup(degrees: Int): Double {
        return sinTable[degrees.mod(MODULUS)]
    }

    fun cosLookup(degrees: Double): Double {
        return sinLookup(((degrees + 90.0) * PRECISION + 0.5).toInt())
    }

    /*
    this is not precise enough for values close to 1 (or -1)
     */
    fun acosLookup(x: Double): Double {
        if (x < -1.0 || x > 1.0) {
            throw IllegalArgumentException("acos only defined between -1 and 1")
        }
        return acosLookup(Math.round(x * ACOS_PRECISION).toInt())
    }

    private fun acosLookup(x: Int): Double {
        return acosTable[x + ACOS_OFFSET]
    }
}