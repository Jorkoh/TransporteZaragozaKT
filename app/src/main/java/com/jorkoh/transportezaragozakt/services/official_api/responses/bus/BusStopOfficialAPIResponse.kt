package com.jorkoh.transportezaragozakt.services.official_api.responses.bus

import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.services.common.responses.BusStopResponse
import com.jorkoh.transportezaragozakt.services.official_api.fixLine
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class BusStopOfficialAPIResponse(
    @Json(name = "id")
    val id: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "destinos")
    val destinos: List<Destino>?,

    @Json(name = "lastUpdated")
    val lastUpdated: Date,

    @Json(name = "icon")
    val icon: String,

    @Json(name = "link")
    val link: String,

    @Json(name = "geometry")
    val geometry: Geometry
) : BusStopResponse {

    override fun toStopDestinations(busStopId: String): List<StopDestination> {
        val stopDestinations = mutableListOf<StopDestination>()
        destinos?.forEach { destination ->
            stopDestinations += StopDestination(
                destination.linea.fixLine(),
                destination.destino.dropLast(1),
                busStopId,
                listOf(
                    destination.primero,
                    destination.segundo
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

    @Json(name = "primero")
    val primero: String,

    @Json(name = "segundo")
    val segundo: String
)

@JsonClass(generateAdapter = true)
data class Geometry(
    @Json(name = "coordinates")
    val coordinates: List<Double>,

    @Json(name = "type")
    val type: String
)