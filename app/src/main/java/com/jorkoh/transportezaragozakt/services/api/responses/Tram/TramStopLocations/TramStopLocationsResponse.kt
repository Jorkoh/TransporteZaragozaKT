package com.jorkoh.transportezaragozakt.services.api.responses.Tram.TramStopLocations

import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.squareup.moshi.Json
import java.util.*

data class TramStopLocationsResponse(
    @field:Json(name = "features")
    val locations: List<Locations>,

    @field:Transient
    @field:Json(name = "type")
    val type: String
)

data class Locations(
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

    @field:Json(name = "stopTitle")
    val title: String,

    @field:Transient
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

data class Geometry(
    @field:Json(name = "coordinates")
    val coordinates: LatLng,

    @field:Transient
    @field:Json(name = "type")
    val type: String
)

data class Destino(
    @field:Transient
    @field:Json(name = "linea")
    val linea: String,

    @field:Transient
    @field:Json(name = "destino")
    val destino: String,

    @field:Transient
    @field:Json(name = "minutos")
    val minutos: Int
)

fun TramStopLocationsResponse.toStops(): List<Stop> {
    val stops = mutableListOf<Stop>()
    locations.forEach { location ->
        stops += Stop(
            StopType.TRAM,
            location.properties.id,
            location.properties.id,
            location.properties.title,
            location.geometry.coordinates,
            listOf("L1"),
            false
        )
    }
    return stops
}