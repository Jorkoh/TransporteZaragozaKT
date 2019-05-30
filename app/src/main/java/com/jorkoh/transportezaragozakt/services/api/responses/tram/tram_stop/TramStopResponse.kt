package com.jorkoh.transportezaragozakt.services.api.responses.tram.tram_stop

import android.content.Context
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class TramStopResponse(
    @Json(name = "features")
    val features: List<Feature>,

    @Json(name = "type")
    val itemType: String
)

@JsonClass(generateAdapter = true)
data class Feature(
    @Json(name = "geometry")
    val geometry: Geometry,

    @Json(name = "properties")
    val properties: Properties,

    @Json(name = "type")
    val type: String
)

@JsonClass(generateAdapter = true)
data class Properties(
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
    val description: String
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
    features.first().properties.destinos?.let { destinations ->
        destinations.forEach { destination ->
            stopDestinations += StopDestination(
                destination.linea,
                destination.destino,
                features.first().properties.id,
                listOf(
                    (features.first().properties.destinos?.getOrNull(0)?.minutos ?: -1).toMinutes(context),
                    (features.first().properties.destinos?.getOrNull(1)?.minutos ?: -1).toMinutes(context)
                ),
                Date()
            )
        }
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