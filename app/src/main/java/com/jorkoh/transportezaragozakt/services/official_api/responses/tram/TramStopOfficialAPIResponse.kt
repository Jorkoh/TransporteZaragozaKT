package com.jorkoh.transportezaragozakt.services.official_api.responses.tram

import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.services.common.responses.TramStopResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class TramStopOfficialAPIResponse(
    @Json(name = "id")
    val stopId: String,

    @Json(name = "uri")
    val link: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "lastUpdated")
    val lastUpdated: Date,

    @Json(name = "mensajes")
    val messages: List<String>,

    @Json(name = "icon")
    val icon: String,

    @Json(name = "destinos")
    val destinos: List<Destino>?,

    @Json(name = "description")
    val description: String,

    @Json(name = "geometry")
    val geometry: Geometry
) : TramStopResponse {

    override fun toStopDestinations(tramStopId: String): List<StopDestination> {
        val stopDestinations = mutableListOf<StopDestination>()
        destinos?.groupBy { it.linea + it.destino }?.forEach { destinationTimes ->
            stopDestinations += StopDestination(
                destinationTimes.value[0].linea,
                destinationTimes.value[0].destino,
                tramStopId,
                listOf(
                    (destinationTimes.value[0].minutos),
                    (destinationTimes.value.getOrNull(1)?.minutos ?: "")
                ),
                listOf("Y", "Y"),
                Date()
            )
        }
        return stopDestinations
    }
}

@JsonClass(generateAdapter = true)
data class Destino(
    @Json(name = "destino")
    val destino: String,

    @Json(name = "linea")
    val linea: String,

    @Json(name = "minutos")
    val minutos: String
)

@JsonClass(generateAdapter = true)
data class Geometry(
    @Json(name = "coordinates")
    val coordinates: List<Double>,

    @Json(name = "type")
    val type: String
)