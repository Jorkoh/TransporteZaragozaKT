package com.jorkoh.transportezaragozakt.db

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json.Companion.parse
import okio.buffer
import okio.source

fun getInitialBusStops(context: Context): InitialStopsMessage {
    val buffer = context.resources.openRawResource(R.raw.initial_bus_stops).source().buffer()
    val busStopsJson = parse(StopsJson.serializer(), buffer.readUtf8())

    val busStopEntities = mutableListOf<Stop>()
    for (stop in busStopsJson.stops) {
        busStopEntities.add(
            Stop(
                StopType.BUS,
                stop.id,
                stop.number,
                stop.title,
                LatLng(stop.location[1], stop.location[0]),
                stop.lines,
                false
            )
        )
    }
    return InitialStopsMessage(busStopEntities, busStopsJson.version)
}

fun getInitialTramStops(context: Context): InitialStopsMessage {
    val buffer = context.resources.openRawResource(R.raw.initial_tram_stops).source().buffer()
    val tramStopsJson = parse(StopsJson.serializer(), buffer.readUtf8())

    val tramStopEntities = mutableListOf<Stop>()
    for (stop in tramStopsJson.stops) {
        tramStopEntities.add(
            Stop(
                StopType.TRAM,
                stop.id,
                stop.number,
                stop.title,
                LatLng(stop.location[1], stop.location[0]),
                stop.lines,
                false
            )
        )
    }
    return InitialStopsMessage(tramStopEntities, tramStopsJson.version)
}

fun getInitialRuralStops(context: Context): InitialStopsMessage {
    val buffer = context.resources.openRawResource(R.raw.initial_rural_stops).source().buffer()
    val ruralStopsJson = parse(StopsJson.serializer(), buffer.readUtf8())

    val ruralStopEntities = mutableListOf<Stop>()
    for (stop in ruralStopsJson.stops) {
        ruralStopEntities.add(
            Stop(
                StopType.RURAL,
                stop.id,
                stop.number,
                stop.title,
                LatLng(stop.location[1], stop.location[0]),
                stop.lines,
                false
            )
        )
    }
    return InitialStopsMessage(ruralStopEntities, ruralStopsJson.version)
}

fun getInitialBusLines(context: Context): InitialLinesMessage {
    val buffer = context.resources.openRawResource(R.raw.initial_bus_lines).source().buffer()
    val busLinesJson = parse(LinesJson.serializer(), buffer.readUtf8())

    val busLinesEntities = mutableListOf<Line>()
    for (line in busLinesJson.lines) {
        busLinesEntities.add(
            Line(
                line.id,
                if(line.parentId.isEmpty()) null else line.parentId,
                LineType.BUS,
                line.nameES,
                line.nameEN,
                line.destinations,
                line.stopsFirstDestination,
                line.stopsSecondDestination
            )
        )
    }
    return InitialLinesMessage(busLinesEntities, busLinesJson.version)
}

fun getInitialTramLines(context: Context): InitialLinesMessage {
    val buffer = context.resources.openRawResource(R.raw.initial_tram_lines).source().buffer()
    val tramLinesJson = parse(LinesJson.serializer(), buffer.readUtf8())

    val tramLinesEntities = mutableListOf<Line>()
    for (line in tramLinesJson.lines) {
        tramLinesEntities.add(
            Line(
                line.id,
                if(line.parentId.isEmpty()) null else line.parentId,
                LineType.TRAM,
                line.nameES,
                line.nameEN,
                line.destinations,
                line.stopsFirstDestination,
                line.stopsSecondDestination
            )
        )
    }
    return InitialLinesMessage(tramLinesEntities, tramLinesJson.version)
}

fun getInitialRuralLines(context: Context): InitialLinesMessage {
    val buffer = context.resources.openRawResource(R.raw.initial_rural_lines).source().buffer()
    val ruralLinesJson = parse(LinesJson.serializer(), buffer.readUtf8())

    val ruralLinesEntities = mutableListOf<Line>()
    for (line in ruralLinesJson.lines) {
        ruralLinesEntities.add(
            Line(
                line.id,
                if(line.parentId.isEmpty()) null else line.parentId,
                LineType.RURAL,
                line.nameES,
                line.nameEN,
                line.destinations,
                line.stopsFirstDestination,
                line.stopsSecondDestination
            )
        )
    }
    return InitialLinesMessage(ruralLinesEntities, ruralLinesJson.version)
}

fun getInitialBusLineLocations(context: Context): InitialLineLocationsMessage {
    val buffer = context.resources.openRawResource(R.raw.initial_bus_lines_locations).source().buffer()
    val busLinesLocationsJson = parse(LinesLocationsJson.serializer(), buffer.readUtf8())

    val busLinesLocationsEntities = mutableListOf<LineLocation>()
    for (line in busLinesLocationsJson.lines) {
        for ((i, location) in line.coordinates.withIndex()) {
            busLinesLocationsEntities.add(
                LineLocation(
                    line.id,
                    LineType.BUS,
                    i + 1,
                    LatLng(location[1], location[0])
                )
            )
        }
    }
    return InitialLineLocationsMessage(busLinesLocationsEntities, busLinesLocationsJson.version)
}

fun getInitialTramLineLocations(context: Context): InitialLineLocationsMessage {
    val buffer = context.resources.openRawResource(R.raw.initial_tram_lines_locations).source().buffer()
    val tramLinesLocationsJson = parse(LinesLocationsJson.serializer(), buffer.readUtf8())

    val tramLinesLocationsEntities = mutableListOf<LineLocation>()
    for (line in tramLinesLocationsJson.lines) {
        for ((i, location) in line.coordinates.withIndex()) {
            tramLinesLocationsEntities.add(
                LineLocation(
                    line.id,
                    LineType.TRAM,
                    i + 1,
                    LatLng(location[1], location[0])
                )
            )
        }
    }
    return InitialLineLocationsMessage(tramLinesLocationsEntities, tramLinesLocationsJson.version)
}

fun getInitialRuralLineLocations(context: Context): InitialLineLocationsMessage {
    val buffer = context.resources.openRawResource(R.raw.initial_rural_lines_locations).source().buffer()
    val ruralLinesLocationsJson = parse(LinesLocationsJson.serializer(), buffer.readUtf8())

    val ruralLinesLocationsEntities = mutableListOf<LineLocation>()
    for (line in ruralLinesLocationsJson.lines) {
        for ((i, location) in line.coordinates.withIndex()) {
            ruralLinesLocationsEntities.add(
                LineLocation(
                    line.id,
                    LineType.RURAL,
                    i + 1,
                    LatLng(location[1], location[0])
                )
            )
        }
    }
    return InitialLineLocationsMessage(ruralLinesLocationsEntities, ruralLinesLocationsJson.version)
}

fun getInitialChangelog(context: Context): InitialChangelog {
    val buffer = context.resources.openRawResource(R.raw.initial_changelog).source().buffer()
    val changelogJson = parse(ChangelogJson.serializer(), buffer.readUtf8())

    return InitialChangelog(changelogJson.textEN, changelogJson.textES, changelogJson.version)
}

data class InitialStopsMessage(val stops: List<Stop>, val version: Int)

data class InitialLinesMessage(val lines: List<Line>, val version: Int)

data class InitialLineLocationsMessage(val lineLocations: List<LineLocation>, val version: Int)

data class InitialChangelog(val textEN: String, val textES: String, val version: Int)

@Serializable
data class StopsJson(val version: Int, val stops: List<StopJson>)

@Serializable
data class LinesJson(val version: Int, val lines: List<LineJson>)

@Serializable
data class LinesLocationsJson(val version: Int, val lines: List<LineLocationsJson>)

@Serializable
data class ChangelogJson(val version: Int, val textEN: String, val textES: String)

@Serializable
data class StopJson(val id: String, val number: String, val title: String, val location: List<Double>, val lines: List<String>)

@Serializable
data class LineJson(
    val id: String,
    val parentId: String,
    val nameES: String,
    val nameEN: String,
    val destinations: List<String>,
    val stopsFirstDestination: List<String>,
    val stopsSecondDestination: List<String>
)

@Serializable
data class LineLocationsJson(val id: String, val coordinates: List<List<Double>>)