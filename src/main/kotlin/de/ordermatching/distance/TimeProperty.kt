package de.ordermatching.distance

import de.ordermatching.model.LogisticsServiceProvider
import de.ordermatching.model.Node
import java.time.OffsetDateTime

class TimeProperty(override val ignoreLongDistance: Boolean = true) : DistanceProperty {

    //preliminary implementation
    override val comparator: Comparator<Node> = Comparator { nodeOne, nodeTwo ->
        if (nodeOne.arrivalTime == null) {
            1
        } else if (nodeTwo.arrivalTime == null) {
            -1
        } else {
            nodeOne.arrivalTime!!.compareTo(nodeTwo.arrivalTime)
        }
    }

    override fun needsUpdate(
        from: Node,
        to: Node,
        arrivalTime: OffsetDateTime,
        emissions: Double,
        price: Double
    ): Boolean {
        return to.arrivalTime == null || arrivalTime.isBefore(to.arrivalTime)
    }
}