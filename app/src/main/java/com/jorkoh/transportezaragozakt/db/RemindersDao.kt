package com.jorkoh.transportezaragozakt.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
abstract class RemindersDao{
    @Insert
    abstract fun insertReminder(reminder: Reminder)

    @Query("SELECT reminders.reminderId, reminders.stopId, reminders.daysOfWeek, reminders.hourOfDay, reminders.minute, stops.type, stops.stopTitle, reminders.alias, reminders.colorHex, stops.lines FROM reminders INNER JOIN stops ON reminders.stopId = stops.stopId ORDER BY reminders.position ASC")
    abstract fun getReminders(): LiveData<List<ReminderExtended>>

    @Query("UPDATE reminders SET daysOfWeek = :daysOfWeek, hourOfDay = :hourOfDay, minute = :minute WHERE reminderId = :reminderId")
    abstract fun updateReminder(reminderId: String, daysOfWeek: ArrayList<Boolean>, hourOfDay: Int, minute: Int)

    @Query("UPDATE reminders SET alias = :alias, colorHex = :colorHex WHERE reminderId = :reminderId")
    abstract fun updateReminder(reminderId: String, colorHex: String, alias: String)

    @Query("DELETE FROM reminders WHERE reminderId = :reminderId")
    abstract fun deleteReminder(reminderId: String)

    @Query("SELECT IFNULL(position, 0)+1 'position' FROM reminders ORDER BY position LIMIT 1")
    abstract fun getLastPositionImmediate(): Int
}