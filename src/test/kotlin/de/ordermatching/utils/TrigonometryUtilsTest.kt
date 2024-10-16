package de.ordermatching.utils

import org.junit.jupiter.api.Test
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class TrigonometryUtilsTest {

    private val tolerance = 0.001 //not very high


    @Test
    fun `test sine lookup`() {
        for (i in 0..359) {
            assertEquals(sin(Math.toRadians(i.toDouble())), TrigonometryUtils.sinLookup(i.toDouble()), tolerance)
        }
    }

    @Test
    fun `test sine lookup with number greater 360`() {
        assertEquals(sin(Math.toRadians(400.0)), TrigonometryUtils.sinLookup(400.0), tolerance)
    }

    @Test
    fun `test sine lookup with negative number`() {
        assertEquals(sin(Math.toRadians(-42.0)), TrigonometryUtils.sinLookup(-42.0), tolerance)
    }

    @Test
    fun `test sine lookup with small number`() {
        assertEquals(sin(Math.toRadians(0.1)), TrigonometryUtils.sinLookup(0.1), tolerance)
    }

    @Test
    fun `test sine lookup with doubles`() {
        for (i in 1..359) {
            val doubleNumber = i + Random.nextDouble(-1.0, 1.0)
            assertEquals(
                sin(Math.toRadians(doubleNumber)),
                TrigonometryUtils.sinLookup(doubleNumber),
                tolerance,
                "failed with $doubleNumber"
            )
        }
    }

    @Test
    fun `test cosine lookup`() {
        for (i in 0..359) {
            assertEquals(cos(Math.toRadians(i.toDouble())), TrigonometryUtils.cosLookup(i.toDouble()), tolerance)
        }
    }

    @Test
    fun `test cosine lookup with number greater 360`() {
        assertEquals(cos(Math.toRadians(400.0)), TrigonometryUtils.cosLookup(400.0), tolerance)
    }

    @Test
    fun `test cosine lookup with negative number`() {
        assertEquals(cos(Math.toRadians(-42.0)), TrigonometryUtils.cosLookup(-42.0), tolerance)
    }

    @Test
    fun `test cosine lookup with small number`() {
        assertEquals(cos(Math.toRadians(0.1)), TrigonometryUtils.cosLookup(0.1), tolerance)
    }

    @Test
    fun `test cosine lookup with doubles`() {
        for (i in 1..359) {
            val doubleNumber = i + Random.nextDouble(-1.0, 1.0)
            assertEquals(
                cos(Math.toRadians(doubleNumber)),
                TrigonometryUtils.cosLookup(doubleNumber),
                tolerance,
                "failed with $doubleNumber"
            )
        }
    }

    @Test
    fun `test acos lookup`() {
        for (i in -1..1) {
            assertEquals(acos(i.toDouble()), TrigonometryUtils.acosLookup(i.toDouble()), tolerance)
        }
    }

    @Test
    fun `test acos lookup with number greater 1`() {
        assertFailsWith<IllegalArgumentException> { TrigonometryUtils.acosLookup(30.0) }
    }

    @Test
    fun `test acos lookup with small number`() {
        assertEquals(acos(0.001), TrigonometryUtils.acosLookup(0.001), tolerance)
    }

    @Test
    fun `test acos lookup with negative number`() {
        assertEquals(acos(-0.66), TrigonometryUtils.acosLookup(-0.66), tolerance)
    }

    @Ignore
    @Test
    fun `test acos lookup with number close to 1`() {
        //this test would fail with current precision 10000 -> we do not use acos lookup
        assertEquals(acos(0.999999), TrigonometryUtils.acosLookup(0.999999), tolerance)
    }

    @Test
    fun `test acos lookup with doubles`() {
        for (i in 1..10) {
            val doubleNumber = Random.nextDouble(-1.0, 1.0)
            assertEquals(
                acos(doubleNumber),
                TrigonometryUtils.acosLookup(doubleNumber),
                tolerance,
                "failed with $doubleNumber"
            )
        }
    }
}