package de.ordermatching.model

data class TransferPoint(
    val position: GeoPosition,
    val owner: LogisticsServiceProvider? = null,
    val openingTimes: List<de.ordermatching.model.Timeslot> = emptyList(), //empty list means no opening time limitations (maybe to null instead)
    val type: TransferPointType = TransferPointType.LOCKER
) {
    override fun toString(): String {
        return "Node(position=$position, type=$type, owner=$owner)"
    }

    /*
    transfer points are equal when same type at exact same position
     */
    override fun equals(other: Any?): Boolean {
        return other != null && other is TransferPoint && other.position == position && other.type == type
    }
}