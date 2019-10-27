package com.jorkoh.transportezaragozakt.db.daos

import androidx.room.*
import com.jorkoh.transportezaragozakt.db.RuralTracking

@Dao
interface TrackingsDao {

    @Query("SELECT * FROM ruralTrackings")
    suspend fun getTrackings(): List<RuralTracking>

    @Query("DELETE FROM ruralTrackings")
    suspend fun deleteTrackings()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackings(ruralTrackings: List<RuralTracking>)

    @Transaction
    suspend fun replaceTrackings(ruralTrackings: List<RuralTracking>){
        deleteTrackings()
        insertTrackings(ruralTrackings)
    }
}