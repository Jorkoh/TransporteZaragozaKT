package com.jorkoh.transportezaragozakt.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    version = 2
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

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("")
    }
}