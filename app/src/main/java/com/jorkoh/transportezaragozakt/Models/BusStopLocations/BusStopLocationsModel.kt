package com.jorkoh.transportezaragozakt.Models.BusStopLocations

import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Json

data class BusStopLocationsModel(
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
    @field:Json(name = "link")
    val link: String,
    @field:Json(name = "title")
    val title: String,

    @field:Transient
    @field:Json(name = "icon")
    val icon: String,
    @Json(name = "id")
    @Transient
    val id: String
)

data class Geometry(
    @field:Json(name = "coordinates")
    val coordinates: LatLng,

    @Transient
    @field:Json(name = "type")
    val type: String
)