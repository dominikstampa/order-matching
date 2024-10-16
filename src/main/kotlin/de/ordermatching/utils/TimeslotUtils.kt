package de.ordermatching.utils

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlin.math.absoluteValue

const val DAYS_PER_WEEK = 7
const val MAX_BUFFER_MINUTES = 30

fun findNextTimeWithinOpeningTimeslot(
    now: OffsetDateTime,
    timeslots: List<de.ordermatching.model.Timeslot>
): OffsetDateTime {
    if (timeslots.isEmpty()) {
        return now
    }
    val nextSlot = findNextOpeningTimeslot(now, timeslots)
    return if (isInTimeslot(now, nextSlot)) {
        now
    } else {
        //is in future
        val daysToGo = (nextSlot.dayOfWeek.ordinal - DayOfWeek.valueOf(now.dayOfWeek.name).ordinal).mod(
            DAYS_PER_WEEK
        )
        now.plusDays(daysToGo.toLong()).withHour(getHour(nextSlot.openTime))
            .withMinute(getMinute(nextSlot.openTime))
    }
}

/*
finds the next timeslot in timeslots that contains now with a given buffer or is after now.
The buffer is applied to the end of the time slot e.g. a buffer of 5 minutes and an end time of 10 means that this timeslot
would only be found for times up until 9:55.
The buffer can also be negative
The buffer cannot be greater than 30 minutes as the buffer should not be larger than a timeslot and is meant to be an
option to prevent finding timslots that are basically already over
 */
fun findNextOpeningTimeslot(
    now: OffsetDateTime,
    timeslots: List<de.ordermatching.model.Timeslot>,
    bufferMinutes: Int = 0
): de.ordermatching.model.Timeslot {
    val timeslot = findNextOpeningTimeslotRestricted(now, timeslots, bufferMinutes, 7)
    require(timeslot != null) //timeslot should never be null
    return timeslot
}

fun findNextOpeningTimeslotRestricted(
    now: OffsetDateTime,
    timeslots: List<de.ordermatching.model.Timeslot>,
    bufferMinutes: Int = 0,
    restrictedToNumDays: Int = 7
): de.ordermatching.model.Timeslot? {
    if (restrictedToNumDays > 7 || restrictedToNumDays < 1) {
        throw IllegalArgumentException()
    }
    if (timeslots.isEmpty() || bufferMinutes.absoluteValue > MAX_BUFFER_MINUTES) {
        throw IllegalArgumentException()
    }
    val bufferCorrectedTime = now.plusMinutes(bufferMinutes.toLong())
    val timeslotComparator =
        compareBy<de.ordermatching.model.Timeslot> {
            (it.dayOfWeek.ordinal - bufferCorrectedTime.dayOfWeek.ordinal).mod(
                DAYS_PER_WEEK
            )
        }.thenBy {
            getHour(
                it.openTime
            )
        }
    val sortedTimeslots = timeslots.sortedWith(timeslotComparator)
    val allowedDays = getNextNDays(now, restrictedToNumDays - 1)
    val timeslotsRestricted =
        sortedTimeslots.filter {
            (allowedDays.contains(it.dayOfWeek) && it.dayOfWeek != now.dayOfWeek) ||
                    it.dayOfWeek == now.dayOfWeek && (getHour(it.closeTime) > bufferCorrectedTime.hour ||
                    (getHour(it.closeTime) == bufferCorrectedTime.hour && getMinute(it.closeTime) >= bufferCorrectedTime.minute))
        }
    if (timeslotsRestricted.isNotEmpty()) {
        return timeslotsRestricted.first()
    }
    if (restrictedToNumDays == DAYS_PER_WEEK) {
        return sortedTimeslots.first()
    }
    //do not return earlier timeslots on same day, maybe restrict days in future even further
    return null
}

fun isInTimeslot(now: OffsetDateTime, timeslot: de.ordermatching.model.Timeslot): Boolean {
    val dayOfWeekNow = now.dayOfWeek
    if (timeslot.dayOfWeek != dayOfWeekNow) {
        return false
    }
    val timeNow = now.toLocalTime()
    val startTime = LocalTime.of(getHour(timeslot.openTime), getMinute(timeslot.openTime))
    val endTime = LocalTime.of(getHour(timeslot.closeTime), getMinute(timeslot.closeTime))
    return (timeNow.isAfter(startTime) || timeNow == startTime)
            && (timeNow.isBefore(endTime) || timeNow == endTime)
}

/*
start and end have to be on the same day
 */
fun timeslotOverlapsStartAndEndOnSameDay(
    timeslot: de.ordermatching.model.Timeslot,
    start: OffsetDateTime,
    end: OffsetDateTime
): Boolean {
    //we do not check for start and end on different days, crowdworker timeslots are always on one day only
    if (start.isAfter(end)) {
        throw IllegalArgumentException("Start time must not be after end time.")
    }
    if (start.toLocalDate() != end.toLocalDate()) {
        throw IllegalArgumentException("Start time and end time must be at the same date.")
    }
    if (timeslot.dayOfWeek != start.dayOfWeek || timeslot.dayOfWeek != end.dayOfWeek) {
        return false
    }
    val timeslotStart = LocalTime.of(getHour(timeslot.openTime), getMinute(timeslot.openTime))
    val timeslotEnd = LocalTime.of(getHour(timeslot.closeTime), getMinute(timeslot.closeTime))
    val startLocalTime = start.toLocalTime()
    val endLocalTime = end.toLocalTime()

    return timeslotStart.isBefore(endLocalTime) && timeslotEnd.isAfter(startLocalTime)
}