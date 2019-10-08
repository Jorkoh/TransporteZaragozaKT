package com.jorkoh.transportezaragozakt.services.ctaz_api.responses.rural

import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.services.common.responses.RuralStopResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class RuralStopCtazAPIResponse(
    @Json(name = "nombre")
    val stopName: String,


    @Json(name = "arrival_time")
    val arrivalTimes: List<ArrivalTime>
) : RuralStopResponse {

    //TODO
    override fun toStopDestinations(ruralStopId: String): List<StopDestination> {
        val stopDestinations = mutableListOf<StopDestination>()
        arrivalTimes.groupBy { it.line + it.lineRoute }.forEach { destinationTimes ->
            stopDestinations += StopDestination(
                destinationTimes.value[0].line,
                "TODO",
                ruralStopId,
                listOf(
                    "TODO",
                    "TODO"
                ),
                Date()
            )
        }
        return stopDestinations
    }
}

@JsonClass(generateAdapter = true)
data class ArrivalTime(
    @Json(name = "id")
    val internalLineId: String,

    @Json(name = "id_linea")
    val line: String,

    @Json(name = "name")
    val lineName: String,

    @Json(name = "route")
    val lineRoute: String,

    @Json(name = "direction")
    val direction: String,

    @Json(name = "bus")
    val vehicleId: String,

    @Json(name = "departure_time")
    val departureTime: Long,

    //TODO
    @Json(name = "remaining_time")
    val arrivalTime: String,

    @Json(name = "TR")
    val TR: String,

    @Json(name = "inc")
    val inc: String,

    @Json(name = "url")
    val url: String
)