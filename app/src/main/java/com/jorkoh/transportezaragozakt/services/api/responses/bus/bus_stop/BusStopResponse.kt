package com.jorkoh.transportezaragozakt.services.api.responses.bus.bus_stop

import android.content.Context
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class BusStopResponse(
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

    @Json(name = "title")
    val title: String,

    @Json(name = "destinos")
    val destinos: List<Destino>?,

    @Json(name = "lastUpdated")
    val lastUpdated: Date,

    @Json(name = "icon")
    val icon: String,

    @Json(name = "link")
    val link: String
)

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

fun BusStopResponse.toStopDestinations(context: Context): List<StopDestination> {
    val stopDestinations = mutableListOf<StopDestination>()
    features.first().properties.destinos?.forEach { destination ->
        stopDestinations += StopDestination(
            destination.linea.correctLine(),
            destination.destino.dropLast(1),
            features.first().properties.id,
            listOf(
                destination.primero.toMinutes(context),
                destination.segundo.toMinutes(context)
            ),
            Date()
        )
    }
    return stopDestinations
}


fun String.toMinutes(context: Context) =
    when (this) {
        "Sin estimacin." -> context.getString(R.string.no_estimate)
        "En la parada." -> context.getString(R.string.at_the_stop)
        else -> {
            val minutes = (this.split(" ")[0].toIntOrNull() ?: -1)
            when (minutes) {
                -1 -> context.getString(R.string.no_estimate)
                1 -> minutes.toString() + " ${context.getString(R.string.minute)}"
                else -> minutes.toString() + " ${context.getString(R.string.minutes)}"
            }
        }
    }

fun String.correctLine() =
    when (this) {
        "CI1" -> "Ci1"
        "CI2" -> "Ci2"
        else -> this
    }