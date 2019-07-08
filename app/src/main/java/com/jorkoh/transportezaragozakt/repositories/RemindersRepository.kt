package com.jorkoh.transportezaragozakt.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.alarms.createAlarms
import com.jorkoh.transportezaragozakt.alarms.deleteAlarms
import com.jorkoh.transportezaragozakt.db.*

interface RemindersRepository {
    fun loadRemindersExtended(): LiveData<List<ReminderExtended>>
    fun insertReminder(stopId: String, stopType: StopType, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int)
    fun updateReminder(reminderId: Int, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int)
    fun updateReminder(reminderId: Int, alias : String, colorHex: String)
    fun restoreReminder(reminderId: Int, stopId : String)
    fun moveReminder(from : Int, to : Int)
    fun deleteReminder(reminderId: Int)
    fun loadReminderAlias(reminderId: Int): LiveData<String>
    fun loadReminderCount(): LiveData<Int>
    fun deleteAllReminders()
}

class RemindersRepositoryImplementation(
    private val remindersDao: RemindersDao,
    private val stopsDao: StopsDao,
    private val db: AppDatabase,
    private val appExecutors: AppExecutors,
    private val context: Context
) : RemindersRepository {
    override fun loadRemindersExtended(): LiveData<List<ReminderExtended>> {
        return remindersDao.getRemindersExtended()
    }

    override fun insertReminder(stopId: String, stopType: StopType, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int) {
        appExecutors.diskIO().execute {
            val reminder = Reminder(0, stopId, stopType, DaysOfWeek(daysOfWeek), hourOfDay, minute, stopsDao.getStopTitleImmediate(stopId), "", remindersDao.getLastPositionImmediate())
            val reminderId =remindersDao.insertReminder(reminder)
            reminder.reminderId = reminderId.toInt()
            reminder.createAlarms(context)
        }
    }

    override fun updateReminder(reminderId: Int, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int) {
        appExecutors.diskIO().execute {
            remindersDao.updateReminder(reminderId, DaysOfWeek(daysOfWeek), hourOfDay, minute)
            remindersDao.getReminderInmediate(reminderId).apply {
                deleteAlarms(context)
                createAlarms(context)
            }
        }
    }

    override fun updateReminder(reminderId: Int, alias: String, colorHex: String) {
        appExecutors.diskIO().execute {
            remindersDao.updateReminder(reminderId, colorHex, alias)
        }
    }

    override fun restoreReminder(reminderId: Int, stopId : String) {
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

    override fun deleteReminder(reminderId: Int) {
        appExecutors.diskIO().execute {
            remindersDao.getReminderInmediate(reminderId).deleteAlarms(context)
            remindersDao.deleteReminder(reminderId)
        }
    }

    override fun loadReminderAlias(reminderId: Int): LiveData<String> {
        return remindersDao.getReminderAlias(reminderId)
    }

    override fun loadReminderCount() : LiveData<Int>{
        return remindersDao.getReminderCount()
    }

    // For testing purposes
    override fun deleteAllReminders() {
        appExecutors.diskIO().execute {
            db.runInTransaction {
                remindersDao.deleteAllReminders()
            }
        }
    }
}