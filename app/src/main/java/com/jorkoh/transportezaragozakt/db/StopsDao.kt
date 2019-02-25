package com.jorkoh.transportezaragozakt.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface StopsDao {
    @Transaction
    @Query("SELECT * FROM stops WHERE id = :stopId")
    fun getStopAndDestinations(stopId: String): LiveData<StopAndDestinations>

    @Query("SELECT * FROM stops WHERE id = :stopId LIMIT 1")
    fun getStop(stopId: String): LiveData<Stop>

    @Query("SELECT * FROM stops WHERE type = :stopType")
    fun getStopsByType(stopType: StopType): LiveData<List<Stop>>

    @Query("SELECT * FROM stopDestinations WHERE stopId = :stopId")
    fun getStopDestinations(stopId: String) : LiveData<List<StopDestination>>

    @Query("SELECT EXISTS(SELECT 1 FROM stopDestinations WHERE stopId = :stopId AND (CAST(strftime('%s', datetime('now', 'localtime')) AS INTEGER) - updatedAt) < :timeoutInSeconds )")
    suspend fun stopHasFreshInfo(stopId: String, timeoutInSeconds: Int) : Boolean

    //TODO: This needs a lot of work
    @Query("SELECT EXISTS(SELECT 1 FROM stops WHERE type = :stopType)")
    suspend fun areStopLocationsSaved(stopType: StopType): Boolean

    @Query("SELECT isFavorite FROM stops WHERE id = :stopId")
    fun stopIsFavorite(stopId: String): LiveData<Boolean>

    @Query("UPDATE stops SET isFavorite = NOT isFavorite WHERE id = :stopId")
    suspend fun toggleStopFavorite(stopId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStop(stop: Stop)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stop: List<Stop>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopDestinations(stopDestinations : List<StopDestination>)
}