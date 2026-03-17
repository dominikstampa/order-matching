package de.ordermatching.model

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class TransferPointTest {

    @MockK
    lateinit var serviceProvider: LogisticsServiceProvider

    private lateinit var tp: TransferPoint

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        tp = TransferPoint(
            position = GeoPosition(1.0, 1.0),
            owner = serviceProvider,
            openingTimes = emptyList(),
            type = TransferPointType.SHOP,
            id = 0
        )
    }

    @Test
    fun `test transfer points not equal with null`() {
        assertFalse(tp.equals(null))
    }

    @Test
    fun `test transfer points not equal with other id`() {
        val other = TransferPoint(
            position = GeoPosition(1.1, 1.0),
            owner = serviceProvider,
            openingTimes = emptyList(),
            type = TransferPointType.SHOP,
            id = 1
        )
        assertNotEquals(tp, other)
    }

    @Test
    fun `test transfer points equal same object`() {
        assertEquals(tp, tp)
    }

    @Test
    fun `test transfer points equal`() {
        val other = TransferPoint(
            position = GeoPosition(1.1, 1.0),
            owner = serviceProvider,
            openingTimes = emptyList(),
            type = TransferPointType.INTERNAL,
            id = 0
        )
        assertEquals(tp, other)
    }

}