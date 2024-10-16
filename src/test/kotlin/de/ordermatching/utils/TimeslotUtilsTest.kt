package de.ordermatching.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.DayOfWeek
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.IllegalArgumentException
import kotlin.test.assertFailsWith

internal class TimeslotUtilsTest {

    private val timeslot = de.ordermatching.model.Timeslot(
        dayOfWeek = DayOfWeek.MONDAY,
        openTime = "10:00",
        closeTime = "12:00"
    )


    @Test
    fun `test is time in timeslot`() {
        val now = OffsetDateTime.of(2023, 4, 21, 12, 0, 0, 0, ZoneOffset.UTC) //Friday
        val timeslot = de.ordermatching.model.Timeslot(
            openTime = "12:00",
            closeTime = "16:00",
            dayOfWeek = DayOfWeek.FRIDAY,
        )

        assert(isInTimeslot(now, timeslot))
    }

    @Test
    fun `test is time in timeslot with other day`() {
        val now = OffsetDateTime.of(2023, 4, 21, 11, 0, 0, 0, ZoneOffset.UTC) //Friday
        val timeslot = de.ordermatching.model.Timeslot(
            openTime = "11:30",
            closeTime = "16:00",
            dayOfWeek = DayOfWeek.THURSDAY,
        )

        assertFalse(isInTimeslot(now, timeslot))
    }

    @Test
    fun `test is time in timeslot with minutes`() {
        val now = OffsetDateTime.of(2023, 4, 21, 11, 50, 0, 0, ZoneOffset.UTC) //Friday
        val timeslot = de.ordermatching.model.Timeslot(
            openTime = "11:30",
            closeTime = "16:00",
            dayOfWeek = DayOfWeek.FRIDAY,
        )
        assert(isInTimeslot(now, timeslot))
    }

    @Test
    fun `test time is not in timeslot`() {
        val now = OffsetDateTime.of(2023, 4, 21, 11, 0, 0, 0, ZoneOffset.UTC) //Friday
        val timeslot = de.ordermatching.model.Timeslot(
            openTime = "11:30",
            closeTime = "16:00",
            dayOfWeek = DayOfWeek.FRIDAY,
        )

        assertFalse(isInTimeslot(now, timeslot))
    }

    @Test
    fun `test time is minutes after timeslot`() {
        val now = OffsetDateTime.of(2023, 4, 21, 16, 10, 0, 0, ZoneOffset.UTC) //Friday
        val timeslot = de.ordermatching.model.Timeslot(
            openTime = "12:00",
            closeTime = "16:00",
            dayOfWeek = DayOfWeek.FRIDAY,
        )
        assertFalse(isInTimeslot(now, timeslot))
    }

    @Test
    fun `test time is minutes before timeslot`() {
        val now = OffsetDateTime.of(2023, 4, 21, 11, 10, 0, 0, ZoneOffset.UTC) //Friday
        val timeslot = de.ordermatching.model.Timeslot(
            openTime = "11:30",
            closeTime = "16:00",
            dayOfWeek = DayOfWeek.FRIDAY,
        )
        assertFalse(isInTimeslot(now, timeslot))
    }

    private fun prepareTimeslotList(): List<de.ordermatching.model.Timeslot> {
        val timeslotMonday = de.ordermatching.model.Timeslot(
            openTime = "09:00",
            closeTime = "18:00",
            dayOfWeek = DayOfWeek.MONDAY,
        )
        val timeslotThursday = de.ordermatching.model.Timeslot(
            openTime = "09:00",
            closeTime = "18:00",
            dayOfWeek = DayOfWeek.THURSDAY,
        )

        val timeslotFridayMorning = de.ordermatching.model.Timeslot(
            openTime = "09:00",
            closeTime = "12:00",
            dayOfWeek = DayOfWeek.FRIDAY,
        )

        val timeslotFridayAfternoon = de.ordermatching.model.Timeslot(
            openTime = "14:00",
            closeTime = "16:00",
            dayOfWeek = DayOfWeek.FRIDAY,
        )

        return listOf(timeslotFridayAfternoon, timeslotFridayMorning, timeslotMonday, timeslotThursday)
    }

    @Test
    fun `test get next timeslot`() {
        val now = OffsetDateTime.of(2023, 4, 17, 10, 0, 0, 0, ZoneOffset.UTC) //Monday
        val timeslots = prepareTimeslotList()

        val next = findNextOpeningTimeslot(now, timeslots)

        assertEquals(DayOfWeek.MONDAY, next.dayOfWeek)
        assertEquals("09:00", next.openTime)
        assertEquals("18:00", next.closeTime)
    }

    @Test
    fun `test get next timeslot with two timeslots at the same day`() {
        val now = OffsetDateTime.of(2023, 4, 21, 14, 30, 0, 0, ZoneOffset.UTC) //Friday
        val timeslots = prepareTimeslotList()

        val next = findNextOpeningTimeslot(now, timeslots)

        assertEquals(DayOfWeek.FRIDAY, next.dayOfWeek)
        assertEquals("14:00", next.openTime)
        assertEquals("16:00", next.closeTime)
    }

    @Test
    fun `test get next timeslot with next timeslots at the same day next week`() {
        val now = OffsetDateTime.of(2023, 12, 4, 14, 30, 0, 0, ZoneOffset.UTC) //Friday
        val timeslots = listOf(timeslot)

        val next = findNextOpeningTimeslot(now, timeslots)

        assertEquals(timeslot, next)
    }

    @Test
    fun `test get next timeslot with next timeslot later on the same day`() {
        val now = OffsetDateTime.of(2023, 4, 21, 12, 30, 0, 0, ZoneOffset.UTC) //Friday
        val timeslots = prepareTimeslotList()

        val next = findNextOpeningTimeslot(now, timeslots)

        assertEquals(DayOfWeek.FRIDAY, next.dayOfWeek)
        assertEquals("14:00", next.openTime)
        assertEquals("16:00", next.closeTime)
    }

    @Test
    fun `test get next timeslot on next day`() {
        val now = OffsetDateTime.of(2023, 4, 20, 20, 0, 0, 0, ZoneOffset.UTC) //Thursday
        val timeslots = prepareTimeslotList()

        val next = findNextOpeningTimeslot(now, timeslots)

        assertEquals(DayOfWeek.FRIDAY, next.dayOfWeek)
        assertEquals("09:00", next.openTime)
        assertEquals("12:00", next.closeTime)
    }

    @Test
    fun `test get next timeslot is first timeslot of the week`() {
        val now = OffsetDateTime.of(2023, 4, 21, 20, 0, 0, 0, ZoneOffset.UTC) //Friday
        val timeslots = prepareTimeslotList()

        val next = findNextOpeningTimeslot(now, timeslots)

        assertEquals(DayOfWeek.MONDAY, next.dayOfWeek)
        assertEquals("09:00", next.openTime)
        assertEquals("18:00", next.closeTime)
    }

    @Test
    fun `test get next timeslot with empty timeslot list`() {
        val now = OffsetDateTime.of(2023, 4, 21, 20, 0, 0, 0, ZoneOffset.UTC) //Friday
        assertFailsWith<IllegalArgumentException> { findNextOpeningTimeslot(now, emptyList()) }
    }

    @Test
    fun `test get next timeslot with positive buffer`() {
        val now = OffsetDateTime.of(2023, 4, 21, 12, 0, 0, 0, ZoneOffset.UTC) //Friday
        val timeslots = prepareTimeslotList()

        //should not find timeslot from 9 to 12
        val next = findNextOpeningTimeslot(now, timeslots, 1)
        assertEquals(DayOfWeek.FRIDAY, next.dayOfWeek)
        assertEquals("14:00", next.openTime)
    }

    @Test
    fun `test get next timeslot with negative buffer`() {
        val now = OffsetDateTime.of(2023, 4, 21, 12, 3, 0, 0, ZoneOffset.UTC) //Friday
        val timeslots = prepareTimeslotList()

        val next = findNextOpeningTimeslot(now, timeslots, -5)
        assertEquals(DayOfWeek.FRIDAY, next.dayOfWeek)
        assertEquals("09:00", next.openTime)
    }

    @Test
    fun `test get next timeslot with too large buffer`() {
        val now = OffsetDateTime.of(2023, 4, 21, 12, 0, 0, 0, ZoneOffset.UTC) //Friday
        val timeslots = prepareTimeslotList()

        assertFailsWith<IllegalArgumentException> { findNextOpeningTimeslot(now, timeslots, 345) }
    }

    @Test
    fun `test find next timeslot with day restriction and no available timeslot on same day`() {
        val now = OffsetDateTime.of(2023, 4, 21, 17, 0, 0, 0, ZoneOffset.UTC) //Friday
        val timeslots = prepareTimeslotList()

        assertNull(findNextOpeningTimeslotRestricted(now, timeslots, 0, 1))
    }

    @Test
    fun `test find next timeslot with day restriction`() {
        val now = OffsetDateTime.of(2023, 4, 21, 17, 0, 0, 0, ZoneOffset.UTC) //Friday
        val timeslots = prepareTimeslotList()

        val next = findNextOpeningTimeslotRestricted(now, timeslots, 0, 4)

        assertNotNull(next)
        assertEquals(DayOfWeek.MONDAY, next!!.dayOfWeek)
    }

    @Test
    fun `test find next timeslot with day restriction too large`() {
        val now = OffsetDateTime.of(2023, 4, 21, 12, 0, 0, 0, ZoneOffset.UTC) //Friday
        val timeslots = prepareTimeslotList()

        assertFailsWith<IllegalArgumentException> { findNextOpeningTimeslotRestricted(now, timeslots, 0, 10) }
    }

    @Test
    fun `test find next time with time in timeslot`() {
        val now = OffsetDateTime.of(2023, 4, 21, 13, 0, 0, 0, ZoneOffset.UTC) //Friday
        val timeslot = de.ordermatching.model.Timeslot(
            openTime = "12:00",
            closeTime = "16:00",
            dayOfWeek = DayOfWeek.FRIDAY
        )

        assertEquals(now, findNextTimeWithinOpeningTimeslot(now, listOf(timeslot)))
    }

    @Test
    fun `test find next time with time not in timeslot`() {
        val now = OffsetDateTime.of(2023, 4, 22, 13, 0, 0, 0, ZoneOffset.UTC) //Friday
        val timeslot = de.ordermatching.model.Timeslot(
            openTime = "12:00",
            closeTime = "16:00",
            dayOfWeek = DayOfWeek.FRIDAY
        )

        assertEquals(
            OffsetDateTime.of(2023, 4, 28, 12, 0, 0, 0, ZoneOffset.UTC),
            findNextTimeWithinOpeningTimeslot(now, listOf(timeslot))
        )
    }

    @Test
    fun `test find next time with empty timeslot list`() {
        val now = OffsetDateTime.of(2023, 4, 22, 13, 0, 0, 0, ZoneOffset.UTC)
        assertEquals(now, findNextTimeWithinOpeningTimeslot(now, emptyList()))
    }

    @Test
    fun `test timeslot between start and end`() {
        val start = OffsetDateTime.of(2023, 11, 20, 9, 0, 0, 0, ZoneOffset.UTC)
        val end = OffsetDateTime.of(2023, 11, 20, 13, 0, 0, 0, ZoneOffset.UTC)

        assert(timeslotOverlapsStartAndEndOnSameDay(timeslot, start, end))
    }

    @Test
    fun `test timeslot before and after start and end`() {
        val start = OffsetDateTime.of(2023, 11, 20, 10, 30, 0, 0, ZoneOffset.UTC)
        val end = OffsetDateTime.of(2023, 11, 20, 11, 30, 0, 0, ZoneOffset.UTC)

        assert(timeslotOverlapsStartAndEndOnSameDay(timeslot, start, end))
    }

    @Test
    fun `test timeslot starts before start and end`() {
        val start = OffsetDateTime.of(2023, 11, 20, 11, 0, 0, 0, ZoneOffset.UTC)
        val end = OffsetDateTime.of(2023, 11, 20, 15, 0, 0, 0, ZoneOffset.UTC)

        assert(timeslotOverlapsStartAndEndOnSameDay(timeslot, start, end))
    }

    @Test
    fun `test timeslot ends after start and end`() {
        val start = OffsetDateTime.of(2023, 11, 20, 9, 0, 0, 0, ZoneOffset.UTC)
        val end = OffsetDateTime.of(2023, 11, 20, 11, 0, 0, 0, ZoneOffset.UTC)

        assert(timeslotOverlapsStartAndEndOnSameDay(timeslot, start, end))
    }

    @Test
    fun `test timeslot between start and end on different day`() {
        val start = OffsetDateTime.of(2023, 11, 21, 9, 0, 0, 0, ZoneOffset.UTC)
        val end = OffsetDateTime.of(2023, 11, 21, 11, 0, 0, 0, ZoneOffset.UTC)

        assertFalse(timeslotOverlapsStartAndEndOnSameDay(timeslot, start, end))
    }

    @Test
    fun `test timeslot between start and end with start and end not on same day`() {
        val start = OffsetDateTime.of(2023, 11, 20, 9, 0, 0, 0, ZoneOffset.UTC)
        val end = OffsetDateTime.of(2023, 11, 21, 11, 0, 0, 0, ZoneOffset.UTC)

        assertFailsWith<IllegalArgumentException> { timeslotOverlapsStartAndEndOnSameDay(timeslot, start, end) }
    }

    @Test
    fun `test timeslot is exactly start and end`() {
        val start = OffsetDateTime.of(2023, 11, 20, 10, 0, 0, 0, ZoneOffset.UTC)
        val end = OffsetDateTime.of(2023, 11, 20, 12, 0, 0, 0, ZoneOffset.UTC)

        assert(timeslotOverlapsStartAndEndOnSameDay(timeslot, start, end))
    }

    @Test
    fun `test timeslot is not between start and end on same day`() {
        val start = OffsetDateTime.of(2023, 11, 20, 12, 0, 0, 0, ZoneOffset.UTC)
        val end = OffsetDateTime.of(2023, 11, 20, 14, 0, 0, 0, ZoneOffset.UTC)

        assertFalse(timeslotOverlapsStartAndEndOnSameDay(timeslot, start, end))
    }

    @Test
    fun `test timeslot between start and end with start after end`() {
        val start = OffsetDateTime.of(2023, 11, 20, 18, 0, 0, 0, ZoneOffset.UTC)
        val end = OffsetDateTime.of(2023, 11, 20, 14, 0, 0, 0, ZoneOffset.UTC)

        assertFailsWith<IllegalArgumentException> { timeslotOverlapsStartAndEndOnSameDay(timeslot, start, end) }
    }
}