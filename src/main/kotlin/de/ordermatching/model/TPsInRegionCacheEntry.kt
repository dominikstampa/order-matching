package de.ordermatching.model

import java.time.LocalDateTime

data class TPsInRegionCacheEntry(
    val timestamp: LocalDateTime,
    val tps: List<TransferPoint>
) {
}