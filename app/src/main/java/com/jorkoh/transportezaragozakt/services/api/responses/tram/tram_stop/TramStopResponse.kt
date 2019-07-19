package com.jorkoh.transportezaragozakt.services.api.responses.tram.tram_stop

import android.content.Context
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class TramStopResponse(
    @Json(name = "id")
    val id: String,

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
)

@JsonClass(generateAdapter = true)
data class Destino(
    @Json(name = "destino")
    val destino: String,

    @Json(name = "linea")
    val linea: String,

    @Json(name = "minutos")
    val minutos: Int
)

@JsonClass(generateAdapter = true)
data class Geometry(
    @Json(name = "coordinates")
    val coordinates: List<Double>,

    @Json(name = "type")
    val type: String
)

fun TramStopResponse.toStopDestinations(context: Context): List<StopDestination> {
    val stopDestinations = mutableListOf<StopDestination>()
    destinos?.groupBy { it.destino }?.forEach { destinationTimes ->
        stopDestinations += StopDestination(
            destinationTimes.value[0].linea,
            destinationTimes.value[0].destino,
            id,
            listOf(
                (destinationTimes.value[0].minutos).toMinutes(context),
                (destinationTimes.value.getOrNull(1)?.minutos ?: -1).toMinutes(context)
            ),
            Date()
        )
    }
    return stopDestinations
}


fun Int.toMinutes(context: Context): String {
    return when (this) {
        -1 -> context.getString(R.string.no_estimate)
        1 -> this.toString() + " ${context.getString(R.string.minute)}"
        else -> this.toString() + " ${context.getString(R.string.minutes)}"
    }
}