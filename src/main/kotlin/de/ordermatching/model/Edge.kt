package de.ordermatching.model

import java.time.OffsetDateTime

class Edge(
    val lsp: LogisticsServiceProvider? = null,
    val possibleCrowdworker: List<Crowdworker>? = null, //either crowdworker or carrier has to be set
    var startTime: OffsetDateTime,
    var endTime: OffsetDateTime,
    val start: Node,
    val end: Node
) {


}