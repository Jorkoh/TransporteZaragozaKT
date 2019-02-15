package com.jorkoh.transportezaragozakt.Models.Bus.BusStop

import com.squareup.moshi.Json

data class BusStopModel(
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

    @field:Json(name = "title")
    val title: String,

    @field:Json(name = "destinos")
    val destinos: List<Destino>,

    @field:Transient
    @field:Json(name = "icon")
    val icon: String,

    @field:Transient
    @field:Json(name = "lastUpdated")
    val lastUpdated: String,

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