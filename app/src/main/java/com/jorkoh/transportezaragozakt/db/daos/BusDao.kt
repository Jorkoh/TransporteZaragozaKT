package com.jorkoh.transportezaragozakt.db.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.jorkoh.transportezaragozakt.db.entities.CompleteStopEntity
import com.jorkoh.transportezaragozakt.db.entities.StopEntity

@Dao
interface BusDao {
    @Transaction
    @Query("SELECT * FROM stops WHERE id = :stopId")
    fun getStop(stopId: String): LiveData<CompleteStopEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM stops WHERE id = :stopId AND (JULIANDAY(CURRENT_TIMESTAMP) - JULIANDAY(updatedAt)) * 86400.0 < :timeoutInSeconds )")
    fun stopHasFreshInfo(stopId: String, timeoutInSeconds: Int) : Boolean

    @Insert
    fun insertStop(stop: StopEntity)
}