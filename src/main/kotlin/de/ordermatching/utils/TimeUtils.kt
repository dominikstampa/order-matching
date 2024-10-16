package de.ordermatching.utils

import java.time.DayOfWeek
import java.time.OffsetDateTime

/**
 * returns the hour value of a time string of the form hh:mm
 */
fun getHour(time: String): Int {
    if (!isValidTimeString(time)) {
        throw IllegalArgumentException("Time string must be in format hh:mm")
    }
    val hour = time.substring(0, 2).toInt()
    if (hour > 23 || hour < 0) {
        throw IllegalArgumentException("Time string must be a valid time in 24 hour format")
    }
    return hour
}

/**
 * returns the minute value of a time string of the form hh:mm
 */
fun getMinute(time: String): Int {
    if (!isValidTimeString(time)) {
        throw IllegalArgumentException("Time string must be in format hh:mm")
    }
    val minute = time.substring(3).toInt()
    if (minute > 59 || minute < 0) {
        throw IllegalArgumentException("Time string must be a valid time in 24 hour format")
    }
    return minute
}

private fun isValidTimeString(time: String): Boolean {
    return time.length == 5 && time[2] == ':'
}

fun getDateFromStartTimeAndDayOfWeek(
    startTime: OffsetDateTime,
    dayOfWeek: DayOfWeek,
    timeString: String
): OffsetDateTime {
    val daysToGo = (dayOfWeek.ordinal - startTime.dayOfWeek.ordinal).mod(
        DAYS_PER_WEEK
    )
    val date = startTime.plusDays(daysToGo.toLong())
    return date.withHour(getHour(timeString)).withMinute(getMinute(timeString))
}

fun getNextNDays(date: OffsetDateTime, n: Int): List<DayOfWeek> {
    if (n < 0) {
        throw IllegalArgumentException("n must not be less than 0.")
    }
    val numberNextDays = if (n > 6) 6 else n
    val daysOfWeek = mutableListOf<DayOfWeek>()
    for (i in 0L..numberNextDays) {
        daysOfWeek.add(date.plusDays(i).dayOfWeek)
    }
    return daysOfWeek
}