package de.ordermatching.model

import java.time.OffsetDateTime

class Node(
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

    override fun toString(): String {
        return "Node(position=$position, type=$type, transferPoint=$transferPoint)"
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is Node && other.position == position && other.type == type && other.lspOwner?.name == lspOwner?.name
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (lspOwner?.name.hashCode() ?: 0)
        return result
    }
}