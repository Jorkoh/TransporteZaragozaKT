package com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStop

import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.services.api.models.StopType
import com.squareup.moshi.Json
import java.util.*

data class BusStopModel(
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
    val destinos: List<Destino>,

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

fun BusStopModel.toStop() = Stop(
    StopType.BUS,
    features.first().properties.id,
    features.first().properties.title,
    LatLng(features.first().geometry.coordinates[0], features.first().geometry.coordinates[1])
)

fun BusStopModel.toStopDestinations() : MutableList<StopDestination> {
    val stopDestinations = mutableListOf<StopDestination>()
    features.first().properties.destinos.forEach { destination ->
        stopDestinations.add(
            StopDestination(
                destination.linea,
                destination.destino,
                features.first().properties.id,
                listOf(destination.primero.toInt(), destination.segundo.toInt()),
                features.first().properties.lastUpdated
            )
        )
    }
    return stopDestinations
}