package com.jorkoh.transportezaragozakt.services.api.responses.Tram.TramStop

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.squareup.moshi.Json
import java.util.*

data class TramStopResponse(
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

    @field:Json(name = "uri")
    val link: String,

    @field:Json(name = "title")
    val title: String,

    @field:Json(name = "lastUpdated")
    val lastUpdated: Date,

    @field:Transient
    @field:Json(name = "mensajes")
    val messages: List<String>,

    @field:Transient
    @field:Json(name = "icon")
    val icon: String,

    @field:Json(name = "destinos")
    val destinos: List<Destino>?,

    @field:Transient
    @field:Json(name = "description")
    val description: String
)

data class Destino(
    @field:Json(name = "destino")
    val destino: String,

    @field:Json(name = "linea")
    val linea: String,

    @field:Json(name = "minutos")
    val minutos: Int
)

data class Geometry(
    @field:Json(name = "coordinates")
    val coordinates: List<Double>,

    @field:Transient
    @field:Json(name = "type")
    val type: String
)

fun TramStopResponse.toStop() = Stop(
    StopType.TRAM,
    features.first().properties.id,
    features.first().properties.title,
    LatLng(features.first().geometry.coordinates[0], features.first().geometry.coordinates[1]),
    false
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