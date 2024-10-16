package de.ordermatching.model

data class TransferPoint(
    val position: GeoPosition,
    val owner: LogisticsServiceProvider? = null,
    val openingTimes: List<de.ordermatching.model.Timeslot> = emptyList(), //empty list means no opening time limitations (maybe to null instead)
    val type: TransferPointType = TransferPointType.LOCKER
) {
}