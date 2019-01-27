package com.jorkoh.transportezaragozakt.Models
import com.squareup.moshi.Json

data class BusStopModel(
    @Json(name = "crs")
    val crs: Crs,
    @Json(name = "features")
    val features: List<Feature>,
    @Json(name = "properties")
    val properties: Properties,
    @Json(name = "type")
    val type: String
)

data class Feature(
    @Json(name = "geometry")
    val geometry: Geometry,
    @Json(name = "properties")
    val properties: Properties,
    @Json(name = "type")
    val type: String
)

data class Properties(
    @Json(name = "destinos")
    val destinos: List<Destino>,
    @Json(name = "icon")
    val icon: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "lastUpdated")
    val lastUpdated: String,
    @Json(name = "link")
    val link: String,
    @Json(name = "title")
    val title: String
)

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

data class Geometry(
    @Json(name = "coordinates")
    val coordinates: List<Double>,
    @Json(name = "type")
    val type: String
)

data class Crs(
    @Json(name = "properties")
    val properties: PropertiesX,
    @Json(name = "type")
    val type: String
)

data class PropertiesX(
    @Json(name = "name")
    val name: String
)

data class PropertiesXX(
    @Json(name = "description")
    val description: String,
    @Json(name = "icon")
    val icon: String,
    @Json(name = "link")
    val link: String,
    @Json(name = "title")
    val title: String
)
