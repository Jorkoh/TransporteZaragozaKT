package com.jorkoh.transportezaragozakt.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jorkoh.transportezaragozakt.db.daos.FavoritesDao
import com.jorkoh.transportezaragozakt.db.daos.RemindersDao
import com.jorkoh.transportezaragozakt.db.daos.StopsDao
import com.jorkoh.transportezaragozakt.db.daos.TrackingsDao

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
    abstract fun favoritesDao(): FavoritesDao
    abstract fun remindersDao(): RemindersDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the new rural trackings table
        database.execSQL("CREATE TABLE ruralTrackings (vehicleId TEXT NOT NULL, lineId TEXT NOT NULL, lineName TEXT NOT NULL, location TEXT NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(vehicleId))")
        // Modify the lines table to add the columns parentLineId, nameES, nameEN and remove the column name
        database.execSQL("CREATE TABLE lines_new (lineId TEXT NOT NULL, parentLineId TEXT, type TEXT NOT NULL, nameES TEXT NOT NULL, nameEN TEXT NOT NULL, destinations TEXT NOT NULL, stopIdsFirstDestination TEXT NOT NULL, stopIdsSecondDestination TEXT NOT NULL, PRIMARY KEY(lineId))")
        database.execSQL("INSERT INTO lines_new (lineId, parentLineId, type, nameES, nameEN, destinations, stopIdsFirstDestination, stopIdsSecondDestination) SELECT lineId, NULL, type, name, name, destinations, stopIdsFirstDestination, stopIdsSecondDestination FROM lines")
        database.execSQL("DROP TABLE lines")
        database.execSQL("ALTER TABLE lines_new RENAME TO lines")
        // Modify the reminders table to remove the foreign key restriction with stops
        database.execSQL("CREATE TABLE reminders_new (reminderId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, stopId TEXT NOT NULL, type TEXT NOT NULL, daysOfWeek TEXT NOT NULL, hourOfDay INTEGER NOT NULL, minute INTEGER NOT NULL, alias TEXT NOT NULL, colorHex TEXT NOT NULL, position INTEGER NOT NULL)")
        database.execSQL("INSERT INTO reminders_new (reminderId, stopId, type, daysOfWeek, hourOfDay, minute, alias, colorHex, position) SELECT reminderId, stopId, type, daysOfWeek, hourOfDay, minute, alias, colorHex, position FROM reminders")
        database.execSQL("DROP TABLE reminders")
        database.execSQL("ALTER TABLE reminders_new RENAME TO reminders")
        database.execSQL("CREATE INDEX index_reminders_stopId ON reminders (stopId)")
        // Modify the stopDestinations (arrival times) table to add the column areTrackedTimes
        database.execSQL("ALTER TABLE stopDestinations ADD COLUMN areTrackedTimes TEXT NOT NULL DEFAULT '[\"Y\",\"Y\"]'")
    }
}