package de.ordermatching

import de.ordermatching.model.Order
import de.ordermatching.model.TransportPlanningResult
import de.ordermatching.distance.DistanceProperty
import de.ordermatching.distance.TimeProperty
import de.ordermatching.planning.DefaultPlanningCalculator
import de.ordermatching.planning.DefaultPlanningInitializer
import de.ordermatching.planning.DefaultPlanningResultGenerator
import de.ordermatching.planning.TransportPlanning

class OrderMatching {

    fun calculateTransport(
        order: Order,
        networkInfo: INetworkInfo,
        config: Configuration = Configuration(),
        distanceProperty: DistanceProperty = TimeProperty()
    ): TransportPlanningResult {
        val planner = TransportPlanning(
            networkInfo,
            distanceProperty,
            config,
            DefaultPlanningInitializer(),
            DefaultPlanningResultGenerator(),
            DefaultPlanningCalculator()
        )
        return planner.calculateTransport(order)
    }
}