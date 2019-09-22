package com.jorkoh.transportezaragozakt.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Stop::class,
        StopDestination::class,
        FavoriteStop::class,
        Reminder::class,
        Line::class,
        LineLocation::class,
        RuralTracking::class
    ],
    exportSchema = false,
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "TransporteZaragozaDB"
    }

    abstract fun stopsDao(): StopsDao
    abstract fun trackingsDao(): TrackingsDao
    abstract fun remindersDao(): RemindersDao
}