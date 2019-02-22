package com.jorkoh.transportezaragozakt.services.api.models.Tram.TramStop

import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.services.api.models.StopType
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
    val destinos: List<Destino>,

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
    LatLng(features.first().geometry.coordinates[0], features.first().geometry.coordinates[1])
)

fun TramStopResponse.toStopDestinations(): List<StopDestination> {
    //TODO: Generate this list without adding?
    val stopDestinations = mutableListOf<StopDestination>()
    features.first().properties.destinos.forEach { destination ->
        stopDestinations.add(
            StopDestination(
                destination.linea,
                destination.destino,
                features.first().properties.id,
                listOf(
                    features.first().properties.destinos.getOrNull(0)?.minutos ?: -1,
                    features.first().properties.destinos.getOrNull(1)?.minutos ?: -1
                ),
                features.first().properties.lastUpdated
            )
        )
    }
    return stopDestinations
}