package com.jorkoh.transportezaragozakt.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.jorkoh.transportezaragozakt.db.DaysOfWeek
import com.jorkoh.transportezaragozakt.db.Reminder
import com.jorkoh.transportezaragozakt.db.ReminderExtended
import com.jorkoh.transportezaragozakt.db.ReminderPositions
import kotlinx.coroutines.flow.Flow

@Dao
interface RemindersDao{
    @Insert
    suspend fun insertReminder(reminder: Reminder) : Long

    @Query("SELECT * FROM reminders where reminderId = :reminderId")
    suspend fun getReminder(reminderId: Int) : Reminder

    @Query("SELECT reminders.reminderId, reminders.stopId, reminders.daysOfWeek, reminders.hourOfDay, reminders.minute, stops.type, stops.stopTitle, reminders.alias, reminders.colorHex, stops.lines FROM reminders INNER JOIN stops ON reminders.stopId = stops.stopId ORDER BY reminders.position ASC")
    fun getRemindersExtended(): Flow<List<ReminderExtended>>

    @Query("SELECT * FROM reminders")
    suspend fun getReminders() : List<Reminder>

    @Query ("SELECT COUNT(*) FROM reminders")
    fun getReminderCount(): Flow<Int>

    @Query("SELECT alias FROM reminders WHERE reminderId = :reminderId")
    fun getReminderAlias(reminderId: Int): Flow<String>

    @Query("UPDATE reminders SET daysOfWeek = :daysOfWeek, hourOfDay = :hourOfDay, minute = :minute WHERE reminderId = :reminderId")
    suspend fun updateReminder(reminderId: Int, daysOfWeek: DaysOfWeek, hourOfDay: Int, minute: Int)

    @Query("UPDATE reminders SET alias = :alias, colorHex = :colorHex WHERE reminderId = :reminderId")
    suspend fun updateReminder(reminderId: Int, colorHex: String, alias: String)

    @Query("DELETE FROM reminders WHERE reminderId = :reminderId")
    suspend fun deleteReminder(reminderId: Int)

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    @Query("SELECT reminderId, position FROM reminders ORDER BY reminders.position ASC")
    suspend fun getReminderPositions(): List<ReminderPositions>

    @Query("UPDATE reminders SET position = :newPosition WHERE reminderId = :reminderId")
    suspend fun updatePosition(reminderId: Int, newPosition: Int)

    @Query("SELECT IFNULL(position, 0)+1 FROM reminders ORDER BY position DESC LIMIT 1")
    fun getLastPosition(): Int

    @Transaction
    suspend fun moveReminder(from: Int, to: Int) {
        val initialPositions = getReminderPositions()
        val finalPositions = initialPositions.toMutableList()

        finalPositions.removeAt(from)
        finalPositions.add(to, initialPositions[from])

        finalPositions.forEachIndexed { index, finalPosition ->
            if (finalPosition != initialPositions[index]) {
                updatePosition(finalPosition.reminderId, index + 1)
            }
        }
    }
}