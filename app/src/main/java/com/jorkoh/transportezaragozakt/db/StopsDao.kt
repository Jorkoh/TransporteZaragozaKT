package com.jorkoh.transportezaragozakt.db

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
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

    @Query("SELECT stopId, position FROM favoriteStops ORDER BY favoriteStops.position ASC")
    abstract fun getFavoritePositions(): List<FavoritePositions>

    @Query("UPDATE favoriteStops SET alias = :alias, colorHex = :colorHex WHERE stopId = :stopId")
    abstract fun updateFavorite(stopId: String, colorHex: String, alias: String)

    //Other stuff
    @Query("SELECT * FROM stops WHERE type = :stopType")
    abstract fun getStopsByType(stopType: StopType): LiveData<List<Stop>>

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

    @Query("DELETE FROM stops")
    abstract fun clearStops()

    @Query("DELETE FROM stopDestinations WHERE stopId = :stopId")
    abstract fun deleteStopDestinations(stopId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertStopDestinations(stopDestinations: List<StopDestination>)

    fun insertInitialData(context: Context) {
        val initialBusStops = getInitialBusStops(context)
        val initialTramStops = getInitialTramStops(context)

        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.preferences_version_number_key),
            Context.MODE_PRIVATE
        )
        with(sharedPreferences.edit()) {
            putInt(context.getString(R.string.saved_bus_version_number_key), initialBusStops.version)
            putInt(context.getString(R.string.saved_bus_version_number_key), initialTramStops.version)
            putInt(context.getString(R.string.map_type_key), 1)
            putBoolean(context.getString(R.string.traffic_key), false)
            putBoolean(context.getString(R.string.bus_filter_key), true)
            putBoolean(context.getString(R.string.tram_filter_key), true)
            commit()
        }
        insertStops(initialBusStops.stops.plus(initialTramStops.stops))
    }

    fun updateStops(stops: List<Stop>) {
        stops.forEach { stop ->
            if (stopIsFavoriteImmediate(stop.stopId)) {
                stop.isFavorite = true
            }
        }
        clearStops()
        insertStops(stops)
    }
}