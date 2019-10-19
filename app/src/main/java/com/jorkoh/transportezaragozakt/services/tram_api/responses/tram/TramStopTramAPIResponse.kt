package com.jorkoh.transportezaragozakt.services.tram_api.responses.tram

import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.services.common.responses.TramStopResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class TramStopTramAPIResponse(
    @Json(name = "stop")
    val stop: String,

    @Json(name = "sense")
    val sense: String,

    @Json(name = "result")
    val result: List<Result>
) : TramStopResponse {

    override fun toStopDestinations(tramStopId: String): List<StopDestination> {
        val stopDestinations = mutableListOf<StopDestination>()
        result.groupBy { it.linea + it.destino }.forEach { destinationTimes ->
            stopDestinations += StopDestination(
                destinationTimes.value[0].linea,
                destinationTimes.value[0].destino,
                tramStopId,
                listOf(
                    (destinationTimes.value[0].minutos.toString()),
                    (destinationTimes.value.getOrNull(1)?.minutos?.toString() ?: "")
                ),
                listOf("Y", "Y"),
                Date()
            )
        }
        return stopDestinations
    }
}

@JsonClass(generateAdapter = true)
data class Result(
    @Json(name = "destino")
    val destino: String,

    @Json(name = "linea")
    val linea: String,

    @Json(name = "minutos")
    val minutos: Int
)