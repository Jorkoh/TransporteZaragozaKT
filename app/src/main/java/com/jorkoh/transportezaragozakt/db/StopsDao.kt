package com.jorkoh.transportezaragozakt.db

import android.content.Context
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jorkoh.transportezaragozakt.R

@Dao
abstract class StopsDao {
    //Favorite stuff
    @Query("SELECT isFavorite FROM stops WHERE stopId = :stopId")
    abstract fun stopIsFavorite(stopId: String): LiveData<Boolean>

    fun toggleFavorite(stopId: String) {
        if (stopIsFavoriteImmediate(stopId)) {
            deleteFavorite(stopId)
            updateIsFavorite(stopId, false)
        } else {
            insertFavoriteStop(FavoriteStop(stopId, getStopTitleImmediate(stopId), "", getLastPositionImmediate()))
            updateIsFavorite(stopId, true)
        }
    }

    fun moveFavorite(from: Int, to: Int) {
        val initialPositions = getFavoritePositions()
        val finalPositions = initialPositions.toMutableList()

        val movedFavorite = finalPositions[from]
        finalPositions.removeAt(from)
        finalPositions.add(to, movedFavorite)

        initialPositions.forEachIndexed { index, oldPosition ->
            if (oldPosition != finalPositions[index]) {
                val newPosition = finalPositions.indexOf(oldPosition) + 1
                updatePosition(oldPosition.stopId, newPosition)
            }
        }
    }

    @Query("UPDATE favoriteStops SET position = :newPosition WHERE stopId = :stopId")
    abstract fun updatePosition(stopId: String, newPosition: Int)

    @Query("SELECT isFavorite FROM stops WHERE stopId = :stopId")
    abstract fun stopIsFavoriteImmediate(stopId: String): Boolean

    @Query("DELETE FROM favoriteStops WHERE stopId = :stopId")
    abstract fun deleteFavorite(stopId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertFavoriteStop(favoriteStop: FavoriteStop)

    @Query("UPDATE stops SET isFavorite = :isFavorite WHERE stopId = :stopId")
    abstract fun updateIsFavorite(stopId: String, isFavorite: Boolean)

    @Query("SELECT favoriteStops.stopId, stops.type, stops.stopTitle, favoriteStops.alias, favoriteStops.colorHex, stops.lines FROM stops INNER JOIN favoriteStops ON stops.stopId = favoriteStops.stopId ORDER BY favoriteStops.position ASC")
    abstract fun getFavoriteStops(): LiveData<List<FavoriteStopExtended>>

    @Query ("SELECT COUNT(*) FROM favoriteStops")
    abstract fun getFavoriteCount(): LiveData<Int>

    @Query("SELECT stopId, position FROM favoriteStops ORDER BY favoriteStops.position ASC")
    abstract fun getFavoritePositions(): List<FavoritePositions>

    @Query("UPDATE favoriteStops SET alias = :alias, colorHex = :colorHex WHERE stopId = :stopId")
    abstract fun updateFavorite(stopId: String, colorHex: String, alias: String)

    //Other stuff
    @Query("SELECT * FROM stops WHERE type = :stopType")
    abstract fun getStopsByType(stopType: StopType): LiveData<List<Stop>>

    @Query("SELECT * FROM lines WHERE type = :lineType")
    abstract fun getLinesByType(lineType: LineType): LiveData<List<Line>>

    @Query("SELECT * FROM lineLocations WHERE lineId = :lineId ORDER BY position ASC")
    abstract fun getLineLocations(lineId: String): LiveData<List<LineLocation>>

    @Query("SELECT * FROM lines WHERE lineId = :lineId")
    abstract fun getLine(lineId: String): LiveData<Line>

    @Query("SELECT * FROM stops ORDER BY type, stopId")
    abstract fun getStops(): LiveData<List<Stop>>

    @Query("SELECT * FROM stops WHERE stopId IN (:stopIds)")
    abstract fun getStops(stopIds: List<String>): LiveData<List<Stop>>

    @Query("SELECT * FROM stopDestinations WHERE stopId = :stopId")
    abstract fun getStopDestinations(stopId: String): LiveData<List<StopDestination>>

    @Query("SELECT stopTitle FROM stops WHERE stopId = :stopId")
    abstract fun getStopTitle(stopId: String): LiveData<String>

    @Query("SELECT stopTitle FROM stops WHERE stopId = :stopId")
    abstract fun getStopTitleImmediate(stopId: String): String

    @Query("SELECT IFNULL(position, 0)+1 'position' FROM favoriteStops ORDER BY position LIMIT 1")
    abstract fun getLastPositionImmediate(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertStops(stop: List<Stop>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertLines(line: List<Line>)

    @Query("DELETE FROM lines where type = :lineType")
    abstract fun clearLines(lineType: LineType)

    @Query("DELETE FROM lineLocations where type = :lineType")
    abstract fun clearLinesLocations(lineType: LineType)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertLinesLocations(line: List<LineLocation>)

    @Query("DELETE FROM stops where type = :stopType")
    abstract fun clearStops(stopType: StopType)

    @Query("DELETE FROM stopDestinations WHERE stopId = :stopId")
    abstract fun deleteStopDestinations(stopId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertStopDestinations(stopDestinations: List<StopDestination>)

    fun insertInitialData(context: Context) {
        val initialBusStops = getInitialBusStops(context)
        val initialTramStops = getInitialTramStops(context)
        val initialBusLines = getInitialBusLines(context)
        val initialTramLines = getInitialTramLines(context)
        val initialBusLineLocations = getInitialBusLineLocations(context)
        val initialTramLineLocations = getInitialTramLineLocations(context)

        with(PreferenceManager.getDefaultSharedPreferences(context).edit()) {
            //This should run before the update data worker, if it doesn't there may be problems with the data versions
            //It should because the data worker forces the database creation and this callback uses the same executor
            putInt(context.getString(R.string.saved_bus_stops_version_number_key), initialBusStops.version)
            putInt(context.getString(R.string.saved_tram_stops_version_number_key), initialTramStops.version)
            putInt(context.getString(R.string.saved_bus_lines_version_number_key), initialBusLines.version)
            putInt(context.getString(R.string.saved_tram_lines_version_number_key), initialTramLines.version)
            putInt(context.getString(R.string.saved_bus_lines_locations_version_number_key), initialBusLineLocations.version)
            putInt(context.getString(R.string.saved_tram_lines_locations_version_number_key), initialTramLineLocations.version)
            putBoolean(context.getString(R.string.is_dark_map_key), false)
            putInt(context.getString(R.string.map_type_key), 1)
            putBoolean(context.getString(R.string.traffic_key), false)
            putBoolean(context.getString(R.string.bus_filter_key), true)
            putBoolean(context.getString(R.string.tram_filter_key), true)
            putInt(context.getString(R.string.search_tab_position_key), 0)
            putBoolean(context.getString(R.string.is_first_launch_key), true)
            apply()
        }
        insertStops(initialBusStops.stops.plus(initialTramStops.stops))
        insertLines(initialBusLines.lines.plus(initialTramLines.lines))
        insertLinesLocations(initialBusLineLocations.lineLocations.plus(initialTramLineLocations.lineLocations))
    }

    fun updateStops(stops: List<Stop>, type : StopType) {
        stops.forEach { stop ->
            if (stopIsFavoriteImmediate(stop.stopId)) {
                stop.isFavorite = true
            }
        }
        clearStops(type)
        insertStops(stops)
    }

    fun updateLines(lines: List<Line>, type : LineType) {
        clearLines(type)
        insertLines(lines)
    }

    fun updateLinesLocations(linesLocations: List<LineLocation>, type : LineType) {
        clearLinesLocations(type)
        insertLinesLocations(linesLocations)
    }
}