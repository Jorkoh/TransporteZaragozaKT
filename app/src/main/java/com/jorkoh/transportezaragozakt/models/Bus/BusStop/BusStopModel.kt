package com.jorkoh.transportezaragozakt.models.Bus.BusStop

import com.jorkoh.transportezaragozakt.models.IStop
import com.jorkoh.transportezaragozakt.models.IStopDestination
import com.squareup.moshi.Json
import java.util.*

data class BusStopModel(
    @field:Json(name = "features")
    val features: List<Feature>,

    @field:Transient
    @field:Json(name = "type")
    val type: String
) : IStop {
    override val title: String
        get() = features.firstOrNull()?.properties?.title ?: ""

    override val destinations: List<IStopDestination>
        get() = features.firstOrNull()?.properties?.destinos ?: listOf()
}

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
    val lastUpdated: Date,

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
) : IStopDestination {
    override val line: String
        get() = linea

    override val destination: String
        get() = destino

    override val times: List<Int>
        get() = listOf(
            primero.split("").first().toIntOrNull() ?: 0,
            segundo.split("").first().toIntOrNull() ?: 0
        )
}

data class Geometry(
    @field:Json(name = "coordinates")
    val coordinates: List<Double>,

    @field:Transient
    @field:Json(name = "type")
    val type: String
)