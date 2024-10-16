package de.ordermatching.networkInfo

import de.ordermatching.AbstractNetworkInfo
import de.ordermatching.model.GeoPosition
import de.ordermatching.model.LogisticsServiceProvider
import de.ordermatching.model.TransferPoint
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import kotlin.random.Random

open class NetworkInfoBaseTest {

    @MockK
    lateinit var networkInfo: AbstractNetworkInfo

    val gf = GeometryFactory()

    val tpCastleKarlsruhe = TransferPoint(GeoPosition(49.01348477660366, 8.404405466354088))
    val tpWildparkKarlsruhe = TransferPoint(GeoPosition(49.02002947593384, 8.413030640497615))
    val tpFZIKarlsruhe = TransferPoint(GeoPosition(49.0118535702568, 8.425350971251904))
    val tpCampusBruchsal = TransferPoint(GeoPosition(49.1170708967543, 8.58796554246431))
    val tpFerryConstance = TransferPoint(GeoPosition(47.682353572508745, 9.210618366773664))

    val allTransferPoints = listOf(
        tpCastleKarlsruhe,
        tpFZIKarlsruhe,
        tpCampusBruchsal,
        tpWildparkKarlsruhe,
        tpFerryConstance
    )

    val lspKarlsruhe = LogisticsServiceProvider(
        name = "KarlsruheLSP",
        externalInteraction = true,
        deliveryRegion = GeometryFactory().createPolygon(
            arrayOf(
                Coordinate(8.298297282090438, 48.98217505562056),
                Coordinate(8.334002847098446, 49.07019754702326),
                Coordinate(8.501887667184175, 49.05287550626616),
                Coordinate(8.472018588764014, 48.96865398670701),
                Coordinate(8.298297282090438, 48.98217505562056)
            )
        )
    )

    val lspConstance = LogisticsServiceProvider(
        name = "KarlsruheLSP",
        externalInteraction = true,
        deliveryRegion = GeometryFactory().createPolygon(
            arrayOf(
                Coordinate(9.116898648072366, 47.70014138147714),
                Coordinate(9.227423191971559, 47.68599798047301),
                Coordinate(9.185398650565022, 47.65373668222781),
                Coordinate(9.14841705412727, 47.666756799016895),
                Coordinate(9.116898648072366, 47.70014138147714)
            )
        )
    )

    open fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    fun generateRandomPolygon(): Polygon {
        val startEnd = Coordinate(Random.nextDouble(6.0, 15.0), Random.nextDouble(47.0, 53.0))
        return GeometryFactory().createPolygon(
            arrayOf(
                startEnd,
                Coordinate(Random.nextDouble(6.0, 15.0), Random.nextDouble(47.0, 53.0)),
                Coordinate(Random.nextDouble(6.0, 15.0), Random.nextDouble(47.0, 53.0)),
                startEnd
            )
        )
    }
}