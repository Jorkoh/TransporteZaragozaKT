package com.jorkoh.transportezaragozakt.db.daos

import android.content.Context
import android.preference.PreferenceManager
import androidx.room.*
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StopsDao {
    @Query("SELECT * FROM stops WHERE type = :stopType")
    fun getStopsByType(stopType: StopType): Flow<List<Stop>>

    @Query("SELECT * FROM lines WHERE type = :lineType AND parentLineId IS NULL")
    fun getMainLinesByType(lineType: LineType): Flow<List<Line>>

    @Query("SELECT * FROM lineLocations WHERE lineId = :lineId ORDER BY position ASC")
    fun getLineLocations(lineId: String): Flow<List<LineLocation>>

    @Query("SELECT * FROM lines WHERE lineId = :lineId")
    fun getLine(lineId: String): Flow<Line>

    @Query("SELECT lineId FROM lines WHERE parentLineId = :lineId")
    fun getAlternativeLineIds(lineId: String): Flow<List<String>>

    @Query("SELECT * FROM stops WHERE stopId = :stopId")
    fun getStop(stopId: String): Flow<Stop>

    @Query("SELECT * FROM stops WHERE stopId IN (:stopIds)")
    fun getStops(stopIds: List<String>): Flow<List<Stop>>

    @Query("SELECT * FROM stopDestinations WHERE stopId = :stopId")
    suspend fun getStopDestinations(stopId: String): List<StopDestination>

    @Query("SELECT stopTitle FROM stops WHERE stopId = :stopId")
    fun getStopTitle(stopId: String): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stop: List<Stop>)

    @Query("SELECT isFavorite FROM stops WHERE stopId = :stopId")
    fun stopIsFavorite(stopId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(line: List<Line>)

    @Query("DELETE FROM lines where type = :lineType")
    suspend fun clearLines(lineType: LineType)

    @Query("DELETE FROM lineLocations where type = :lineType")
    fun clearLinesLocations(lineType: LineType)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinesLocations(line: List<LineLocation>)

    @Query("DELETE FROM stops where type = :stopType")
    suspend fun clearStops(stopType: StopType)

    @Query("DELETE FROM stopDestinations WHERE stopId = :stopId")
    suspend fun deleteStopDestinations(stopId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopDestinations(stopDestinations: List<StopDestination>)

    @Transaction
    suspend fun replaceStopDestinations(stopId: String, stopDestinations: List<StopDestination>){
        deleteStopDestinations(stopId)
        insertStopDestinations(stopDestinations)
    }

    @Transaction
    suspend fun insertInitialData(context: Context) {
        val initialChangelog = getInitialChangelog(context)
        with(PreferenceManager.getDefaultSharedPreferences(context).edit()) {
            // Versions of data
            putInt(
                context.getString(R.string.saved_bus_stops_version_number_key),
                context.resources.getInteger(R.integer.bus_stops_default_version)
            )
            putInt(
                context.getString(R.string.saved_tram_stops_version_number_key),
                context.resources.getInteger(R.integer.tram_stops_default_version)
            )
            putInt(
                context.getString(R.string.saved_rural_stops_version_number_key),
                context.resources.getInteger(R.integer.rural_stops_default_version)
            )
            putInt(
                context.getString(R.string.saved_bus_lines_version_number_key),
                context.resources.getInteger(R.integer.bus_lines_default_version)
            )
            putInt(
                context.getString(R.string.saved_tram_lines_version_number_key),
                context.resources.getInteger(R.integer.tram_lines_default_version)
            )
            putInt(
                context.getString(R.string.saved_rural_lines_version_number_key),
                context.resources.getInteger(R.integer.rural_lines_default_version)
            )
            putInt(
                context.getString(R.string.saved_bus_lines_locations_version_number_key),
                context.resources.getInteger(R.integer.bus_lines_locations_default_version)
            )
            putInt(
                context.getString(R.string.saved_tram_lines_locations_version_number_key),
                context.resources.getInteger(R.integer.tram_lines_locations_default_version)
            )
            putInt(
                context.getString(R.string.saved_rural_lines_locations_version_number_key),
                context.resources.getInteger(R.integer.rural_lines_locations_default_version)
            )
            putInt(
                context.getString(R.string.saved_changelog_version_number_key),
                context.resources.getInteger(R.integer.changelog_default_version)
            )
            // Changelog
            putString(context.getString(R.string.saved_changelog_en_key), initialChangelog.textEN)
            putString(context.getString(R.string.saved_changelog_es_key), initialChangelog.textES)
            // Default settings
            putBoolean(context.getString(R.string.is_dark_map_key), false)
            putInt(context.getString(R.string.map_type_key), 1)
            putBoolean(context.getString(R.string.traffic_key), false)
            putBoolean(context.getString(R.string.map_animations_key), true)
            putBoolean(context.getString(R.string.bus_filter_key), true)
            putBoolean(context.getString(R.string.tram_filter_key), true)
            putBoolean(context.getString(R.string.rural_filter_key), false)
            putInt(context.getString(R.string.search_tab_position_key), 0)
            putBoolean(context.getString(R.string.is_first_launch_key), true)

            apply()
        }

        // Insert initial data
        val initialBusStops = getInitialStops(context, R.raw.initial_bus_stops, StopType.BUS)
        val initialTramStops = getInitialStops(context, R.raw.initial_tram_stops, StopType.TRAM)
        val initialRuralStops = getInitialStops(context, R.raw.initial_rural_stops, StopType.RURAL)

        val initialBusLines = getInitialLines(context, R.raw.initial_bus_lines, LineType.BUS)
        val initialTramLines = getInitialLines(context, R.raw.initial_tram_lines, LineType.TRAM)
        val initialRuralLines = getInitialLines(context, R.raw.initial_rural_lines, LineType.RURAL)

        val initialBusLineLocations = getInitialLineLocations(context, R.raw.initial_bus_lines_locations, LineType.BUS)
        val initialTramLineLocations = getInitialLineLocations(context, R.raw.initial_tram_lines_locations, LineType.TRAM)
        val initialRuralLineLocations = getInitialLineLocations(context, R.raw.initial_rural_lines_locations, LineType.RURAL)

        insertStops(initialBusStops.plus(initialTramStops).plus(initialRuralStops))
        insertLines(initialBusLines.plus(initialTramLines).plus(initialRuralLines))
        insertLinesLocations(initialBusLineLocations.plus(initialTramLineLocations).plus(initialRuralLineLocations))
    }

    @Transaction
    suspend fun updateStops(stops: List<Stop>, type: StopType) {
        stops.forEach { stop ->
            if (stopIsFavorite(stop.stopId)) {
                stop.isFavorite = true
            }
        }
        clearStops(type)
        insertStops(stops)
    }

    @Transaction
    suspend fun updateLines(lines: List<Line>, type: LineType) {
        clearLines(type)
        insertLines(lines)
    }

    @Transaction
    suspend fun updateLinesLocations(linesLocations: List<LineLocation>, type: LineType) {
        clearLinesLocations(type)
        insertLinesLocations(linesLocations)
    }
}