package com.jorkoh.transportezaragozakt.db

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json.Companion.parse
import okio.Okio

fun getInitialBusStops(context: Context) : InitialStopsMessage {
    val buffer = Okio.buffer(Okio.source(context.resources.openRawResource(R.raw.initial_bus_stops)))
    val busStopsJson = parse(StopsJson.serializer(), buffer.readUtf8())

    val busStopEntities = mutableListOf<Stop>()
    for (stop in busStopsJson.stops){
        busStopEntities.add(Stop(StopType.BUS, stop.id, stop.title, LatLng(stop.location[1], stop.location[0]), false))
    }
    return InitialStopsMessage(busStopEntities, busStopsJson.version.toInt())
}

fun getInitialTramStops(context: Context) : InitialStopsMessage {
    val buffer = Okio.buffer(Okio.source(context.resources.openRawResource(R.raw.initial_tram_stops)))
    val tramStopsJson = parse(StopsJson.serializer(), buffer.readUtf8())

    val tramStopEntities = mutableListOf<Stop>()
    for (stop in tramStopsJson.stops){
        tramStopEntities.add(Stop(StopType.TRAM, stop.id, stop.title, LatLng(stop.location[1], stop.location[0]), false))
    }
    return InitialStopsMessage(tramStopEntities, tramStopsJson.version)
}

data class InitialStopsMessage(val stops:List<Stop>, val version : Int)

@Serializable
data class StopsJson(val version: Int, val recordedAt: String, val stops: List<StopJson>)

@Serializable
data class StopJson(val id: String, val title: String, val location : List<Double>, val lines : List<String>)