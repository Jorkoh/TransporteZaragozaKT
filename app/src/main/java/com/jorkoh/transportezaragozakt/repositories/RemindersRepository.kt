package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.*

interface RemindersRepository {
    fun loadReminders(): LiveData<List<ReminderExtended>>
    fun insertReminder(stopId: String, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int)
    fun updateReminder(reminderId: String, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int)
    fun updateReminder(reminderId: String, alias : String, colorHex: String)
    fun restoreReminder(reminderId: String, stopId : String)
    fun moveReminder(from : Int, to : Int)
    fun deleteReminder(reminderId: String)
}

class RemindersRepositoryImplementation(
    private val remindersDao: RemindersDao,
    private val stopsDao: StopsDao,
    private val appExecutors: AppExecutors
) : RemindersRepository {
    override fun loadReminders(): LiveData<List<ReminderExtended>> {
        return remindersDao.getReminders()
    }

    override fun insertReminder(stopId: String, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int) {
        appExecutors.diskIO().execute {
            remindersDao.insertReminder(Reminder(-1, stopId, ArrayList(daysOfWeek), hourOfDay, minute, stopsDao.getStopTitleImmediate(stopId), "", remindersDao.getLastPositionImmediate()))
        }
    }

    override fun updateReminder(reminderId: String, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int) {
        appExecutors.diskIO().execute {
            remindersDao.updateReminder(reminderId, ArrayList(daysOfWeek), hourOfDay, minute)
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteReminder(reminderId: String) {
        appExecutors.diskIO().execute {
            remindersDao.deleteReminder(reminderId)
        }
    }
}