package com.jorkoh.transportezaragozakt.models.Tram.TramStopLocations

import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.models.IStopLocation
import com.squareup.moshi.Json
import java.util.*

data class TramStopLocationsModel(
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
): IStopLocation {
    override val stopId: String
        get() = properties.id
    override val coordinates: LatLng
        get() = geometry.coordinates
}

data class Properties(
    @field:Json(name = "id")
    val id: String,

    @field:Json(name = "uri")
    val link: String,

    @field:Json(name = "title")
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