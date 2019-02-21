package com.jorkoh.transportezaragozakt.db.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jorkoh.transportezaragozakt.db.entities.StopEntity
import com.jorkoh.transportezaragozakt.models.IStop

@Dao
interface TramDao{
    @Query("SELECT * FROM stops WHERE id = :stopId")
    fun getStop(stopId : String) : LiveData<StopEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStop(stop : StopEntity)
}