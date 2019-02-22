package com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStopLocations

import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.services.api.models.IStopLocation
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
) : IStopLocation {
    override val stopId: String
        get() = properties.id
    override val coordinates: LatLng
        get() = geometry.coordinates
}

data class Properties(
    @field:Json(name = "id")
    val id: String,
    @field:Json(name = "title")
    val title: String,
    @field:Json(name = "link")
    val link: String,

    @field:Transient
    @field:Json(name = "icon")
    val icon: String
)

data class Geometry(
    @field:Json(name = "coordinates")
    val coordinates: LatLng,

    @Transient
    @field:Json(name = "type")
    val type: String
)