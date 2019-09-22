package com.jorkoh.transportezaragozakt.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class TrackingsDao {

    @Query("SELECT * FROM ruralTrackings")
    abstract fun getTrackings(): LiveData<List<RuralTracking>>

    @Query("DELETE FROM ruralTrackings")
    abstract fun deleteTrackings()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTrackings(ruralTrackings: List<RuralTracking>)
}