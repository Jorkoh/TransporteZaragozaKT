package com.jorkoh.transportezaragozakt.Models.Tram.TramStop

import com.jorkoh.transportezaragozakt.Models.Tram.TramStopLocations.Destino
import com.squareup.moshi.Json
import java.util.*

data class TramStopModel(
    @field:Json(name = "features")
    val features: List<Feature>,

    @field:Transient
    @field:Json(name = "type")
    val type: String
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
