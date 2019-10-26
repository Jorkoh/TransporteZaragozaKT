package com.jorkoh.transportezaragozakt.db.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jorkoh.transportezaragozakt.db.DaysOfWeek
import com.jorkoh.transportezaragozakt.db.Reminder
import com.jorkoh.transportezaragozakt.db.ReminderExtended
import com.jorkoh.transportezaragozakt.db.ReminderPositions

@Dao
abstract class RemindersDao{
    @Insert
    abstract fun insertReminder(reminder: Reminder) : Long

    @Query("SELECT * FROM reminders where reminderId = :reminderId")
    abstract fun getReminderInmediate(reminderId: Int) : Reminder

    @Query("SELECT reminders.reminderId, reminders.stopId, reminders.daysOfWeek, reminders.hourOfDay, reminders.minute, stops.type, stops.stopTitle, reminders.alias, reminders.colorHex, stops.lines FROM reminders INNER JOIN stops ON reminders.stopId = stops.stopId ORDER BY reminders.position ASC")
    abstract fun getRemindersExtended(): LiveData<List<ReminderExtended>>

    @Query("SELECT * FROM reminders")
    abstract fun getRemindersImmediate() : List<Reminder>

    @Query ("SELECT COUNT(reminderId) FROM reminders")
    abstract fun getReminderCount(): LiveData<Int>

    @Query("SELECT alias FROM reminders WHERE reminderId = :reminderId")
    abstract fun getReminderAlias(reminderId: Int): LiveData<String>

    @Query("UPDATE reminders SET daysOfWeek = :daysOfWeek, hourOfDay = :hourOfDay, minute = :minute WHERE reminderId = :reminderId")
    abstract fun updateReminder(reminderId: Int, daysOfWeek: DaysOfWeek, hourOfDay: Int, minute: Int)

    @Query("UPDATE reminders SET alias = :alias, colorHex = :colorHex WHERE reminderId = :reminderId")
    abstract fun updateReminder(reminderId: Int, colorHex: String, alias: String)

    @Query("DELETE FROM reminders WHERE reminderId = :reminderId")
    abstract fun deleteReminder(reminderId: Int)

    @Query("DELETE FROM reminders")
    abstract fun deleteAllReminders()

    @Query("SELECT IFNULL(position, 0)+1 'position' FROM reminders ORDER BY position LIMIT 1")
    abstract fun getLastPositionImmediate(): Int

    @Query("SELECT reminderId, position FROM reminders ORDER BY reminders.position ASC")
    abstract fun getReminderPositions(): List<ReminderPositions>

    @Query("UPDATE reminders SET position = :newPosition WHERE reminderId = :reminderId")
    abstract fun updatePosition(reminderId: Int, newPosition: Int)

    fun moveReminder(from: Int, to: Int) {
        val initialPositions = getReminderPositions()
        val finalPositions = initialPositions.toMutableList()

        val movedReminder = finalPositions[from]
        finalPositions.removeAt(from)
        finalPositions.add(to, movedReminder)

        initialPositions.forEachIndexed { index, oldPosition ->
            if (oldPosition != finalPositions[index]) {
                val newPosition = finalPositions.indexOf(oldPosition) + 1
                updatePosition(oldPosition.reminderId, newPosition)
            }
        }
    }
}