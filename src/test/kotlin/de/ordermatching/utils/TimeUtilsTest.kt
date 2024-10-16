package de.ordermatching.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertFailsWith

internal class TimeUtilsTest {

    @Test
    fun `test get hour with valid time strings`() {
        Assertions.assertEquals(10, getHour("10:32"))
        Assertions.assertEquals(0, getHour("00:00"))
        Assertions.assertEquals(21, getHour("21:00"))
    }

    @Test
    fun `test get hour with wrong format time strings`() {
        assertThrows<IllegalArgumentException> {
            getHour("0:0")
        }
        assertThrows<IllegalArgumentException> {
            getHour("0123456")
        }
        assertThrows<IllegalArgumentException> {
            getHour("10:2")
        }
    }

    @Test
    fun `test get hour with string not convertible to number`() {
        assertThrows<NumberFormatException> {
            getHour("tl:dr")
        }
    }

    @Test
    fun `test get hour with invalid times`() {
        assertThrows<IllegalArgumentException> {
            getHour("30:21")
        }
    }

    @Test
    fun `get minute with valid time string`() {
        Assertions.assertEquals(32, getMinute("10:32"))
        Assertions.assertEquals(0, getMinute("00:00"))
        Assertions.assertEquals(7, getMinute("21:07"))
    }

    @Test
    fun `test get minute with wrong format time strings`() {
        assertThrows<IllegalArgumentException> {
            getMinute("0:0")
        }
        assertThrows<IllegalArgumentException> {
            getMinute("0123456")
        }
        assertThrows<IllegalArgumentException> {
            getMinute("10:2")
        }
    }

    @Test
    fun `test get minute with string not convertible to number`() {
        assertThrows<NumberFormatException> {
            getMinute("tl:dr")
        }
    }

    @Test
    fun `test get minute with invalid times`() {
        assertThrows<IllegalArgumentException> {
            getMinute("14:74")
        }
    }

    @Test
    fun `test get date from start time and day of week`() {
        val startDate = OffsetDateTime.of(2023, 9, 26, 13, 0, 0, 0, ZoneOffset.UTC) //TUESDAY
        val tuesday = DayOfWeek.TUESDAY
        val time = "15:30"
        val expectedDate = OffsetDateTime.of(2023, 9, 26, 15, 30, 0, 0, ZoneOffset.UTC)
        Assertions.assertEquals(expectedDate, getDateFromStartTimeAndDayOfWeek(startDate, tuesday, time))
    }

    @Test
    fun `test get date from start date and day of week`() {
        val startDate = OffsetDateTime.of(2023, 9, 26, 13, 0, 0, 0, ZoneOffset.UTC) //TUESDAY
        val thursday = DayOfWeek.THURSDAY
        val time = "15:30"
        val expectedDate = OffsetDateTime.of(2023, 9, 28, 15, 30, 0, 0, ZoneOffset.UTC)
        Assertions.assertEquals(expectedDate, getDateFromStartTimeAndDayOfWeek(startDate, thursday, time))
    }

    @Test
    fun `test get date from start date and day of week in next week`() {
        val startDate = OffsetDateTime.of(2023, 9, 26, 13, 0, 0, 0, ZoneOffset.UTC) //TUESDAY
        val thursday = DayOfWeek.MONDAY
        val time = "15:30"
        val expectedDate = OffsetDateTime.of(2023, 10, 2, 15, 30, 0, 0, ZoneOffset.UTC)
        Assertions.assertEquals(expectedDate, getDateFromStartTimeAndDayOfWeek(startDate, thursday, time))
    }

    @Test
    fun `test get date from start date and day of week in next year`() {
        val startDate = OffsetDateTime.of(2023, 12, 30, 13, 0, 0, 0, ZoneOffset.UTC) //SATURDAY
        val thursday = DayOfWeek.THURSDAY
        val time = "15:30"
        val expectedDate = OffsetDateTime.of(2024, 1, 4, 15, 30, 0, 0, ZoneOffset.UTC)
        Assertions.assertEquals(expectedDate, getDateFromStartTimeAndDayOfWeek(startDate, thursday, time))
    }

    @Test
    fun `test get next 0 days`() {
        val tuesdayTime = OffsetDateTime.of(2023, 11, 14, 13, 0, 0, 0, ZoneOffset.UTC)
        val listOfDays = getNextNDays(tuesdayTime, 0)
        Assertions.assertEquals(1, listOfDays.size)
        Assertions.assertEquals(DayOfWeek.TUESDAY, listOfDays.first())
    }

    @Test
    fun `test get next 3 days`() {
        val tuesdayTime = OffsetDateTime.of(2023, 11, 14, 13, 0, 0, 0, ZoneOffset.UTC) //TUESDAY
        val listOfDays = getNextNDays(tuesdayTime, 3)
        Assertions.assertEquals(4, listOfDays.size)
        Assertions.assertEquals(DayOfWeek.TUESDAY, listOfDays[0])
        Assertions.assertEquals(DayOfWeek.WEDNESDAY, listOfDays[1])
        Assertions.assertEquals(DayOfWeek.THURSDAY, listOfDays[2])
        Assertions.assertEquals(DayOfWeek.FRIDAY, listOfDays[3])
    }

    @Test
    fun `test get next 4 days at end of week`() {
        val fridayTime = OffsetDateTime.of(2023, 11, 17, 13, 0, 0, 0, ZoneOffset.UTC)
        val listOfDays = getNextNDays(fridayTime, 4)
        Assertions.assertEquals(5, listOfDays.size)
        Assertions.assertEquals(DayOfWeek.FRIDAY, listOfDays[0])
        Assertions.assertEquals(DayOfWeek.SATURDAY, listOfDays[1])
        Assertions.assertEquals(DayOfWeek.SUNDAY, listOfDays[2])
        Assertions.assertEquals(DayOfWeek.MONDAY, listOfDays[3])
        Assertions.assertEquals(DayOfWeek.TUESDAY, listOfDays[4])
    }

    @Test
    fun `test get next 8 days only returns all 7 days of the week`() {
        val tuesdayTime = OffsetDateTime.of(2023, 11, 14, 13, 0, 0, 0, ZoneOffset.UTC)
        val listOfDays = getNextNDays(tuesdayTime, 8)
        Assertions.assertEquals(7, listOfDays.size)
    }

    @Test
    fun `test get next days with negative number`() {
        val tuesdayTime = OffsetDateTime.of(2023, 11, 14, 13, 0, 0, 0, ZoneOffset.UTC)
        assertFailsWith<IllegalArgumentException> { getNextNDays(tuesdayTime, -1) }
    }
}