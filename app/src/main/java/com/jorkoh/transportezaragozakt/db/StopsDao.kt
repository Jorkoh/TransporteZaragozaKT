package com.jorkoh.transportezaragozakt.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface StopsDao {
    @Transaction
    @Query("SELECT * FROM stops WHERE id = :stopId")
    fun getStopAndDestinations(stopId: String): LiveData<StopAndDestinations>

    @Query("SELECT * FROM stops WHERE id = :stopId")
    fun getStop(stopId: String): LiveData<Stop>

    @Query("SELECT * FROM stopDestinations WHERE stopId = :stopId")
    fun getStopDestinations(stopId: String) : LiveData<List<StopDestination>>

    @Query("SELECT EXISTS(SELECT 1 FROM stopDestinations WHERE stopId = :stopId AND (JULIANDAY(CURRENT_TIMESTAMP) - JULIANDAY(updatedAt)) * 86400.0 < :timeoutInSeconds )")
    fun stopHasFreshInfo(stopId: String, timeoutInSeconds: Int) : Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStop(stop: Stop)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStopDestinations(stopDestinations : List<StopDestination>)
}