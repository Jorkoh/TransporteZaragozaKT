package com.jorkoh.transportezaragozakt.db

import android.content.Context
import androidx.core.app.BundleCompat
import androidx.lifecycle.LiveData
import androidx.room.*
import com.jorkoh.transportezaragozakt.R

@Dao
abstract class StopsDao {
    @Query("SELECT * FROM stops WHERE type = :stopType")
    abstract fun getStopsByType(stopType: StopType): LiveData<List<Stop>>

    @Query("SELECT * FROM stopDestinations WHERE stopId = :stopId")
    abstract fun getStopDestinations(stopId: String): LiveData<List<StopDestination>>

    @Query("SELECT isFavorite FROM stops WHERE id = :stopId")
    abstract fun stopIsFavorite(stopId: String): LiveData<Boolean>

    fun toggleFavorite(stopId: String) {
        if (stopIsFavoriteImmediate(stopId)) {
            deleteFavorite(stopId)
            updateIsFavorite(stopId, false)
        } else {
            insertFavoriteStop(FavoriteStop(stopId, "000000"))
            updateIsFavorite(stopId, true)
        }
    }

    @Query("SELECT title FROM stops WHERE id = :stopId")
    abstract fun getStopTitle(stopId: String): LiveData<String>

    @Query("SELECT isFavorite FROM stops WHERE id = :stopId")
    abstract fun stopIsFavoriteImmediate(stopId: String): Boolean

    @Query("DELETE FROM favoriteStops WHERE stopId = :stopId")
    abstract fun deleteFavorite(stopId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertFavoriteStop(favoriteStop: FavoriteStop)

    @Query("UPDATE stops SET isFavorite = :isFavorite WHERE id = :stopId")
    abstract fun updateIsFavorite(stopId: String, isFavorite: Boolean)

    @Query("SELECT stops.* FROM stops INNER JOIN favoriteStops ON stops.id = favoriteStops.stopId")
    abstract fun getFavoriteStops(): LiveData<List<Stop>>

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

        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preferences_version_number_key), Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt(context.getString(R.string.saved_bus_version_number_key), initialBusStops.version)
            putInt(context.getString(R.string.saved_bus_version_number_key), initialTramStops.version)
            putInt(context.getString(R.string.map_type_key), 1)
            putBoolean(context.getString(R.string.bus_filter_key), true)
            putBoolean(context.getString(R.string.tram_filter_key), true)
            commit()
        }
        insertStops(initialBusStops.stops.plus(initialTramStops.stops))
    }

    fun updateStops(stops : List<Stop>){
        stops.forEach {stop ->
            if(stopIsFavoriteImmediate(stop.id)){
                stop.isFavorite = true
            }
        }
        clearStops()
        insertStops(stops)
    }
}