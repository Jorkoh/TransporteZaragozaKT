package com.jorkoh.transportezaragozakt.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class StopsDao {
//    @Transaction
//    @Query("SELECT * FROM stops WHERE id = :stopId")
//    fun getStopAndDestinations(stopId: String): LiveData<StopAndDestinations>
//
//    @Query("SELECT * FROM stops WHERE id = :stopId LIMIT 1")
//    fun getStop(stopId: String): LiveData<Stop>
//
//    @Query("SELECT * FROM stops")
//    fun getStops(): LiveData<List<Stop>>

    @Query("SELECT * FROM stops WHERE type = :stopType")
    abstract fun getStopsByType(stopType: StopType): LiveData<List<Stop>>

    @Query("SELECT * FROM stopDestinations WHERE stopId = :stopId")
    abstract fun getStopDestinations(stopId: String): LiveData<List<StopDestination>>

//    @Query("SELECT EXISTS(SELECT 1 FROM stopDestinations WHERE stopId = :stopId AND (CAST(strftime('%s', datetime('now', 'localtime')) AS INTEGER) - updatedAt) < :timeoutInSeconds )")
//    fun stopHasFreshInfo(stopId: String, timeoutInSeconds: Int): Boolean

//    @Query("SELECT EXISTS(SELECT 1 FROM stops)")
//    fun areStopLocationsSaved(): Boolean
//
//    //TODO: This needs a lot of work
//    @Query("SELECT EXISTS(SELECT 1 FROM stops WHERE type = :stopType)")
//    fun areStopLocationsSaved(stopType: StopType): Boolean

    @Query("SELECT isFavorite FROM stops WHERE id = :stopId")
    abstract fun stopIsFavorite(stopId: String): LiveData<Boolean>

    /**/
    fun toggleFavorite(stopId: String){
        if(stopIsFavoriteImmediate(stopId)){
            deleteFavorite(stopId)
            updateIsFavorite(stopId, false)
        }else{
            insertFavoriteStop(FavoriteStop(stopId, "000000"))
            updateIsFavorite(stopId, true)
        }
    }

    @Query("SELECT isFavorite FROM stops WHERE id = :stopId")
    abstract fun stopIsFavoriteImmediate(stopId: String): Boolean

    @Query("DELETE FROM favoriteStops WHERE stopId = :stopId")
    abstract fun deleteFavorite(stopId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertFavoriteStop(favoriteStop: FavoriteStop)

    @Query("UPDATE stops SET isFavorite = :isFavorite WHERE id = :stopId")
    abstract fun updateIsFavorite(stopId: String, isFavorite: Boolean)
    /**/

    @Query("SELECT stops.* FROM stops INNER JOIN favoriteStops ON stops.id = favoriteStops.stopId")
    abstract fun getFavoriteStops(): LiveData<List<Stop>>

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertStop(stop: Stop)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertStops(stop: List<Stop>)

    ////////
    @Query("DELETE FROM stopDestinations WHERE stopId = :stopId")
    abstract fun deleteStopDestinations(stopId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertStopDestinations(stopDestinations: List<StopDestination>)
}