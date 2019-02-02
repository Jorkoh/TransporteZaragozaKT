package com.jorkoh.transportezaragozakt.Models.BusStopLocations
import com.jorkoh.transportezaragozakt.Models.BusStop.Properties
import com.squareup.moshi.Json

data class BusStopLocationsModel(
    @Json(name = "features")
    val features: List<Feature>,
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
    @Json(name = "icon")
    val icon: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "link")
    val link: String,
    @Json(name = "title")
    val title: String
)

data class Geometry(
    @Json(name = "coordinates")
    val coordinates: List<Double>,
    @Json(name = "type")
    val type: String
)