package de.ordermatching.model

import java.time.DayOfWeek

data class Timeslot(
    val dayOfWeek: DayOfWeek,
    val openTime: String,
    val closeTime: String
) {

}