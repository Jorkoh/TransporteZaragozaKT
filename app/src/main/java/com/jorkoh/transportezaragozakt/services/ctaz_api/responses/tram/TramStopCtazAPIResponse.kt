package com.jorkoh.transportezaragozakt.services.ctaz_api.responses.tram

import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.services.common.responses.TramStopResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class TramStopCtazAPIResponse(
    @Json(name = "tram_time")
    val tram_time: List<TramTime>
) : TramStopResponse {

    override fun toStopDestinations(tramStopId: String): List<StopDestination> {
        val stopDestinations = mutableListOf<StopDestination>()
        tram_time.groupBy { it.line + it.destination }.forEach { destinationTimes ->
            stopDestinations += StopDestination(
                destinationTimes.value[0].line,
                destinationTimes.value[0].destination,
                tramStopId,
                listOf(
                    (destinationTimes.value[0].arrival_time.toString()),
                    (destinationTimes.value.getOrNull(1)?.arrival_time?.toString() ?: "")
                ),
                Date()
            )
        }
        return stopDestinations
    }
}

@JsonClass(generateAdapter = true)
data class TramTime(
    @Json(name = "line")
    val line: String,

    @Json(name = "destination")
    val destination: String,

    @Json(name = "arrival_time")
    val arrival_time: Int
)