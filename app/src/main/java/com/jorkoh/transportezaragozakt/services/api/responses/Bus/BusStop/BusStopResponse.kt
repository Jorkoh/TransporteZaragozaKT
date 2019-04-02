package com.jorkoh.transportezaragozakt.services.api.responses.Bus.BusStop

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.squareup.moshi.Json
import java.util.*

data class BusStopResponse(
    @field:Json(name = "features")
    val features: List<Feature>,

    @field:Transient
    @field:Json(name = "type")
    val itemType: String
)

data class Feature(
    @field:Json(name = "geometry")
    val geometry: Geometry,

    @field:Json(name = "properties")
    val properties: Properties,

    @field:Transient
    @field:Json(name = "type")
    val type: String
)

data class Properties(
    @field:Json(name = "id")
    val id: String,

    @field:Json(name = "title")
    val title: String,

    @field:Json(name = "destinos")
    val destinos: List<Destino>?,

    @field:Json(name = "lastUpdated")
    val lastUpdated: Date,

    @field:Transient
    @field:Json(name = "icon")
    val icon: String,

    @field:Transient
    @field:Json(name = "link")
    val link: String
)

data class Destino(
    @field:Json(name = "destino")
    val destino: String,

    @field:Json(name = "linea")
    val linea: String,

    @field:Json(name = "primero")
    val primero: String,

    @field:Json(name = "segundo")
    val segundo: String
)

data class Geometry(
    @field:Json(name = "coordinates")
    val coordinates: List<Double>,

    @field:Transient
    @field:Json(name = "type")
    val type: String
)

fun BusStopResponse.toStop(): Stop = Stop(
    StopType.BUS,
    features.first().properties.id,
    features.first().properties.title,
    LatLng(features.first().geometry.coordinates[0], features.first().geometry.coordinates[1]),
    false
)

fun BusStopResponse.toStopDestinations(context: Context): List<StopDestination> {
    val stopDestinations = mutableListOf<StopDestination>()
    features.first().properties.destinos?.forEach { destination ->
        stopDestinations += StopDestination(
            destination.linea,
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


fun String.toMinutes(context: Context): String {
    return when (this) {
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
}