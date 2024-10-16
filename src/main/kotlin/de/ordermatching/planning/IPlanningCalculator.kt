package de.ordermatching.planning

import de.ordermatching.model.PlanningInput

interface IPlanningCalculator {

    fun calculate(input: PlanningInput)
}