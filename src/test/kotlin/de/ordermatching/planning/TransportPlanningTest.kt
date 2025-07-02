package de.ordermatching.planning

import de.ordermatching.Configuration
import de.ordermatching.INetworkInfo
import de.ordermatching.distance.TimeProperty
import de.ordermatching.helper.testLsp
import de.ordermatching.helper.testOrder
import de.ordermatching.model.GeoPosition
import de.ordermatching.model.ServiceInfo
import de.ordermatching.model.TransferPoint
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/*
Test whole planning component with default implementations
 */
internal class TransportPlanningTest {

    @MockK
    private lateinit var networkInfo: INetworkInfo

    private val timeProperty = TimeProperty()
    private val config = Configuration()
    private val initializer = DefaultPlanningInitializer()
    private val resultGenerator = DefaultPlanningResultGenerator()
    private val planningCalculator = DefaultPlanningCalculator()

    private val transferPoint = TransferPoint(GeoPosition(50.0, 10.0))
    private val serviceInfo = ServiceInfo(
        priceEstimate = 3.0,
        deliveryDateEstimate = testOrder.startTime.plusDays(1),
        emissionsEstimate = 5.0
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        setUpMocks()
    }

    @Test
    fun calculateTransport() {
        val planning =
            TransportPlanning(networkInfo, timeProperty, config, initializer, resultGenerator, planningCalculator)
        val result = planning.calculateTransport(testOrder)

        assertEquals(1, result.transportSteps.size)
    }

    private fun setUpMocks() {
        every { networkInfo.findNearestTransferPoints(testOrder.senderPosition) } returns listOf(transferPoint)
        every { networkInfo.getAllTransferPoints() } returns listOf(transferPoint)
        every { networkInfo.findLSPsWithPositionInDeliveryRegion(transferPoint.position) } returns listOf(testLsp)
        every { networkInfo.findTransferPointsInLSPDeliveryRegion(testLsp) } returns listOf(transferPoint)
        every { networkInfo.getServiceInfo(testLsp, any(), any(), any(), any()) } returns serviceInfo
        every { networkInfo.findSuitedCWRoutesNearPosition(any(), any()) } returns emptyList()
    }

    @Test
    fun calculateTransportTwice() {
        val planning =
            TransportPlanning(networkInfo, timeProperty, config, initializer, resultGenerator, planningCalculator)
        val result = planning.calculateTransport(testOrder)

        assertEquals(1, result.transportSteps.size)

        val result2 = planning.calculateTransport(testOrder)

        assertEquals(1, result2.transportSteps.size)
    }
}
