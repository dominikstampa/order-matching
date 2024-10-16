package de.ordermatching.distance

import de.ordermatching.model.LogisticsServiceProvider
import de.ordermatching.model.Node
import java.time.OffsetDateTime

class EmissionProperty(override val ignoreLongDistance: Boolean = true) : DistanceProperty {

    override val comparator: Comparator<Node> = Comparator { nodeOne, nodeTwo ->
        if (nodeOne.emissions == null) {
            1
        } else if (nodeTwo.emissions == null) {
            -1
        } else {
            nodeOne.emissions!!.compareTo(nodeTwo.emissions!!)
        }
    }

    override fun needsUpdate(
        from: Node,
        to: Node,
        arrivalTime: OffsetDateTime,
        emissions: Double,
        price: Double,
        lsp: LogisticsServiceProvider?
    ): Boolean {
        val estimatedEmissions = estimateEmissions(from.position, to.position, emissions)
        return to.emissions == null || from.emissions!! + estimatedEmissions < to.emissions!!
    }
}