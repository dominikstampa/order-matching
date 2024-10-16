package de.ordermatching.distance

import de.ordermatching.model.LogisticsServiceProvider
import de.ordermatching.model.Node
import java.time.OffsetDateTime

class PriceProperty(override val ignoreLongDistance: Boolean = true) : DistanceProperty {

    override val comparator: Comparator<Node> = Comparator { nodeOne, nodeTwo ->
        if (nodeOne.price == null) {
            1
        } else if (nodeTwo.price == null) {
            -1
        } else {
            nodeOne.price!!.compareTo(nodeTwo.price!!)
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
        return to.price == null || to.price!! > from.price!! + price
    }

}