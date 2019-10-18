package com.jorkoh.transportezaragozakt.db

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json.Companion.parse
import okio.buffer
import okio.source

fun getInitialStops(context: Context, rawResource: Int, stopType: StopType): MutableList<Stop> {
    val buffer = context.resources.openRawResource(rawResource).source().buffer()
    val stopsJson = parse(StopsJson.serializer(), buffer.readUtf8())

    val stopEntities = mutableListOf<Stop>()
    for (stop in stopsJson.stops) {
        stopEntities.add(
            Stop(
                stopType,
                stop.id,
                stop.number,
                stop.title,
                LatLng(stop.location[1], stop.location[0]),
                stop.lines,
                false
            )
        )
    }
    return stopEntities
}

fun getInitialLines(context: Context, rawResource: Int, lineType: LineType): MutableList<Line> {
    val buffer = context.resources.openRawResource(rawResource).source().buffer()
    val linesJson = parse(LinesJson.serializer(), buffer.readUtf8())

    val linesEntities = mutableListOf<Line>()
    for (line in linesJson.lines) {
        linesEntities.add(
            Line(
                line.id,
                if(line.parentId.isEmpty()) null else line.parentId,
                lineType,
                line.nameES,
                line.nameEN,
                line.destinations,
                line.stopsFirstDestination,
                line.stopsSecondDestination
            )
        )
    }
    return linesEntities
}

fun getInitialLineLocations(context: Context, rawResource: Int, lineType: LineType): MutableList<LineLocation> {
    val buffer = context.resources.openRawResource(rawResource).source().buffer()
    val linesLocationsJson = parse(LinesLocationsJson.serializer(), buffer.readUtf8())

    val linesLocationsEntities = mutableListOf<LineLocation>()
    for (line in linesLocationsJson.lines) {
        for ((i, location) in line.coordinates.withIndex()) {
            linesLocationsEntities.add(
                LineLocation(
                    line.id,
                    lineType,
                    i + 1,
                    LatLng(location[1], location[0])
                )
            )
        }
    }
    return linesLocationsEntities
}

fun getInitialChangelog(context: Context): ChangelogJson {
    val buffer = context.resources.openRawResource(R.raw.initial_changelog).source().buffer()
    return parse(ChangelogJson.serializer(), buffer.readUtf8())
}

@Serializable
data class StopsJson(val stops: List<StopJson>)

@Serializable
data class LinesJson(val lines: List<LineJson>)

@Serializable
data class LinesLocationsJson(val lines: List<LineLocationsJson>)

@Serializable
data class ChangelogJson(val textEN: String, val textES: String)

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