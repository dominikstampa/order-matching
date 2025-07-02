package de.ordermatching.helper

import de.ordermatching.AbstractNetworkInfo
import de.ordermatching.model.*
import java.time.OffsetDateTime
import kotlin.random.Random

/*
This class was made to test the default implementations of AbstractNetworkInfo.
As some of them use private fields, testing cannot be done by mocking it with MockK, so we use this dummy implementation.
 */
class NetworkInfoMockImplementation : AbstractNetworkInfo() {

    private var allTps = emptyList<TransferPoint>()
    private var allLsps = emptyList<LogisticsServiceProvider>()
    private var allCws = emptyList<Crowdworker>()

    fun setAllTpsMock(allTps: List<TransferPoint>) {
        this.allTps = allTps
    }

    fun setAllLspsMock(allLsps: List<LogisticsServiceProvider>) {
        this.allLsps = allLsps
    }

    fun setAllCws(allCws: List<Crowdworker>) {
        this.allCws = allCws
    }

    override fun getAllTransferPoints(): List<TransferPoint> {
        return allTps
    }

    override fun getAllLogisticsServiceProvider(): List<LogisticsServiceProvider> {
        return allLsps
    }

    override fun getServiceInfo(
        parcelService: LogisticsServiceProvider,
        startTime: OffsetDateTime,
        packageSize: PackageSize,
        startPosition: GeoPosition,
        endPosition: GeoPosition,
    ): ServiceInfo {
        return ServiceInfo(
            priceEstimate = 2.0,
            deliveryDateEstimate = startTime.plusDays(1),
            emissionsEstimate = Random.nextDouble(0.0, 30.0)
        )
    }

    override fun getAllCrowdworker(): List<Crowdworker> {
        return allCws
    }
}