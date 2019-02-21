package com.jorkoh.transportezaragozakt.models.Tram.TramStop

import com.jorkoh.transportezaragozakt.models.IStop
import com.jorkoh.transportezaragozakt.models.IStopDestination
import com.jorkoh.transportezaragozakt.models.StopType
import com.squareup.moshi.Json
import java.util.*

data class TramStopModel(
    @field:Json(name = "features")
    val features: List<Feature>,

    @field:Transient
    @field:Json(name = "type")
    val itemType: String
) : IStop {
    override val type: StopType = StopType.TRAM

    override val id: String
        get() = features.firstOrNull()?.properties?.id ?: ""

    override val title: String
        get() = features.firstOrNull()?.properties?.title ?: ""

    override val destinations: List<IStopDestination>
        get() = features.firstOrNull()?.properties?.destinations ?: listOf()
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
) {
    val destinations: List<IStopDestination>
        get() = listOf(
            object : IStopDestination {
                override val line: String = destinos.firstOrNull()?.linea ?: ""
                override val destination: String = destinos.firstOrNull()?.destino ?: ""
                override val times: List<Int> = listOf(
                    destinos.firstOrNull()?.minutos ?: 0,
                    destinos.lastOrNull()?.minutos ?: 0
                )
            }
        )
}

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
