package com.jorkoh.transportezaragozakt.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.alarms.createAlarm
import com.jorkoh.transportezaragozakt.db.*

interface RemindersRepository {
    fun loadReminders(): LiveData<List<ReminderExtended>>
    fun insertReminder(stopId: String, stopType: StopType, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int)
    fun updateReminder(reminderId: String, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int)
    fun updateReminder(reminderId: String, alias : String, colorHex: String)
    fun restoreReminder(reminderId: String, stopId : String)
    fun moveReminder(from : Int, to : Int)
    fun deleteReminder(reminderId: String)
}

class RemindersRepositoryImplementation(
    private val remindersDao: RemindersDao,
    private val stopsDao: StopsDao,
    private val db: AppDatabase,
    private val appExecutors: AppExecutors,
    private val context: Context
) : RemindersRepository {
    override fun loadReminders(): LiveData<List<ReminderExtended>> {
        return remindersDao.getReminders()
    }

    override fun insertReminder(stopId: String, stopType: StopType, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int) {
        appExecutors.diskIO().execute {
            val reminder = Reminder(0, stopId, stopType, DaysOfWeek(daysOfWeek), hourOfDay, minute, stopsDao.getStopTitleImmediate(stopId), "", remindersDao.getLastPositionImmediate())
            val reminderId =remindersDao.insertReminder(reminder)
            reminder.reminderId = reminderId.toInt()
            reminder.createAlarm(context)
        }
    }

    override fun updateReminder(reminderId: String, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int) {
        appExecutors.diskIO().execute {
            //TODO REDO ALARM
            remindersDao.updateReminder(reminderId, DaysOfWeek(daysOfWeek), hourOfDay, minute)
        }
    }

    override fun updateReminder(reminderId: String, alias: String, colorHex: String) {
        appExecutors.diskIO().execute {
            remindersDao.updateReminder(reminderId, colorHex, alias)
        }
    }

    override fun restoreReminder(reminderId: String, stopId : String) {
        appExecutors.diskIO().execute {
            remindersDao.updateReminder(reminderId, "", stopsDao.getStopTitleImmediate(stopId))
        }
    }

    override fun moveReminder(from: Int, to: Int) {
        appExecutors.diskIO().execute {
            db.runInTransaction {
                remindersDao.moveReminder(from, to)
            }
        }
    }

    override fun deleteReminder(reminderId: String) {
        appExecutors.diskIO().execute {
            //TODO DELETE ALARM
            remindersDao.deleteReminder(reminderId)
        }
    }
}