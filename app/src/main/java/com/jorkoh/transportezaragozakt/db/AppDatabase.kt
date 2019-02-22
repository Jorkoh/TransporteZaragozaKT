package com.jorkoh.transportezaragozakt.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jorkoh.transportezaragozakt.db.entities.StopDestinationEntity
import com.jorkoh.transportezaragozakt.db.entities.StopEntity

@Database(entities = [StopEntity::class, StopDestinationEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase(){
    companion object {
        const val DATABASE_NAME = "TransporteZaragozaDB"
    }

    abstract fun busDao() : StopsDao
    abstract fun tramDao() : TramDao
}