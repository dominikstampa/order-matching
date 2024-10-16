package de.ordermatching.model

import java.time.OffsetDateTime

data class Node(
    val position: GeoPosition,
    val transferPoint: TransferPoint?, //null of no transfer point (start/end)
    val lspOwner: LogisticsServiceProvider?, //null if lbn transfer point
    val type: NodeType,
    var predecessor: Node? = null,
    var lspToNode: LogisticsServiceProvider? = null, //lsp that transports to this node, null if transport is done by crowdworker
    var arrivalTime: OffsetDateTime? = null,
    var emissions: Double? = null,
    var price: Double? = null,
    var hasLongDistanceTransport: Boolean = false //TODO remove for package?
) {
}