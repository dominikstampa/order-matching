package de.ordermatching.helper

import de.ordermatching.model.*
import kotlin.random.Random

fun getEndNode(order: Order): Node {
    return Node(
        position = order.recipientPosition,
        transferPoint = null,
        lspOwner = null,
        type = NodeType.END
    )
}

fun getStartNode(order: Order): Node {
    return Node(
        position = order.senderPosition,
        transferPoint = TransferPoint(order.senderPosition),
        lspOwner = null,
        type = NodeType.NEUTRAL,
        predecessor = null,
        lspToNode = null,
        arrivalTime = order.startTime,
        emissions = 0.0,
        price = 0.0
    )
}

/*
    returns a random position roughly in the borders of Germany
     */
fun getRandomPositionGermany(): GeoPosition {
    return GeoPosition(Random.nextDouble(47.0, 54.0), Random.nextDouble(6.0, 15.0))
}

fun getRandomPositionInRect(
    bottomLeft: GeoPosition = GeoPosition(47.0, 6.0),
    topRight: GeoPosition = GeoPosition(54.0, 15.0)
): GeoPosition {
    return GeoPosition(
        Random.nextDouble(bottomLeft.latitude, topRight.latitude),
        Random.nextDouble(bottomLeft.longitude, topRight.longitude)
    )
}