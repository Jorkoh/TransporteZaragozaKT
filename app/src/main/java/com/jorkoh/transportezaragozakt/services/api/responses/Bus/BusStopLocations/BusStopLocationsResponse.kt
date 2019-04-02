package com.jorkoh.transportezaragozakt.services.api.responses.Bus.BusStopLocations

import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.squareup.moshi.Json

data class BusStopLocationsResponse(
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

fun BusStopLocationsResponse.toStops(): List<Stop> {
    val stops = mutableListOf<Stop>()
    locations.forEach { location ->
        stops += Stop(
            StopType.BUS,
            location.properties.id,
            location.properties.title,
            location.geometry.coordinates,
            false
        )
    }
    return stops
}