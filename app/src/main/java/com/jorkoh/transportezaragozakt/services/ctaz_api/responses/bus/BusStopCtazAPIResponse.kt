package com.jorkoh.transportezaragozakt.services.ctaz_api.responses.bus

import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.services.common.responses.BusStopResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.fixLine
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class BusStopCtazAPIResponse(
    @Json(name = "urban_arrival_time")
    val urban_arrival_times: List<UrbanTime>
) : BusStopResponse {

    override fun toStopDestinations(busStopId: String): List<StopDestination> {
        val stopDestinations = mutableListOf<StopDestination>()
        urban_arrival_times
            .filterNot { it.line == "Sin información" || it.destination == "Sin destino" }
            .groupBy { it.line + it.destination }
            .forEach { destinationTimes ->
                stopDestinations += StopDestination(
                    destinationTimes.value[0].line.fixLine(),
                    destinationTimes.value[0].destination,
                    busStopId,
                    listOf(
                        (destinationTimes.value[0].arrival_time),
                        (destinationTimes.value.getOrNull(1)?.arrival_time ?: "")
                    ),
                    Date()
                )
            }
        return stopDestinations
    }
}

@JsonClass(generateAdapter = true)
data class UrbanTime(
    @Json(name = "line")
    val line: String,

    @Json(name = "destination")
    val destination: String,

    @Json(name = "arrival_time")
    val arrival_time: String,

    @Json(name = "narrival_time")
    val narrival_time: Int?
)

