package com.jorkoh.transportezaragozakt.repositories

import android.content.Context
import com.jorkoh.transportezaragozakt.alarms.createAlarms
import com.jorkoh.transportezaragozakt.alarms.deleteAlarms
import com.jorkoh.transportezaragozakt.db.DaysOfWeek
import com.jorkoh.transportezaragozakt.db.Reminder
import com.jorkoh.transportezaragozakt.db.ReminderExtended
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.db.daos.RemindersDao
import com.jorkoh.transportezaragozakt.db.daos.StopsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface RemindersRepository {
    fun getRemindersExtended(): Flow<List<ReminderExtended>>
    fun getReminderCount(): Flow<Int>
    fun getReminderAlias(reminderId: Int): Flow<String>
    suspend fun updateReminder(reminderId: Int, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int)
    suspend fun updateReminder(reminderId: Int, alias: String, colorHex: String)
    suspend fun insertReminder(stopId: String, stopType: StopType, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int)
    suspend fun restoreReminder(reminder: ReminderExtended)
    suspend fun moveReminder(from: Int, to: Int)
    suspend fun removeReminder(reminderId: Int)
    suspend fun deleteAllReminders()
}

class RemindersRepositoryImplementation(
    private val remindersDao: RemindersDao,
    private val stopsDao: StopsDao,
    private val context: Context
) : RemindersRepository {
    override fun getRemindersExtended(): Flow<List<ReminderExtended>> {
        return remindersDao.getRemindersExtended()
    }

    override fun getReminderCount(): Flow<Int> {
        return remindersDao.getReminderCount()
    }

    override fun getReminderAlias(reminderId: Int): Flow<String> {
        return remindersDao.getReminderAlias(reminderId)
    }

    override suspend fun updateReminder(reminderId: Int, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int) {
        remindersDao.updateReminder(reminderId, DaysOfWeek(daysOfWeek), hourOfDay, minute)
        remindersDao.getReminder(reminderId).apply {
            deleteAlarms(context)
            createAlarms(context)
        }
    }

    override suspend fun updateReminder(reminderId: Int, alias: String, colorHex: String) {
        remindersDao.updateReminder(reminderId, colorHex, alias)
    }

    override suspend fun insertReminder(stopId: String, stopType: StopType, daysOfWeek: List<Boolean>, hourOfDay: Int, minute: Int) {
        val reminder = Reminder(
            0,
            stopId,
            stopType,
            DaysOfWeek(daysOfWeek),
            hourOfDay,
            minute,
            withContext(Dispatchers.IO) { stopsDao.getStopTitle(stopId) },
            "",
            withContext(Dispatchers.IO) { remindersDao.getLastPosition() }
        )
        val reminderId = remindersDao.insertReminder(reminder)
        reminder.reminderId = reminderId.toInt()
        reminder.createAlarms(context)
    }

    override suspend fun restoreReminder(reminder: ReminderExtended) {
        remindersDao.updateReminder(reminder.reminderId, "", reminder.stopTitle)
    }

    override suspend fun moveReminder(from: Int, to: Int) {
        remindersDao.moveReminder(from, to)
    }

    override suspend fun removeReminder(reminderId: Int) {
        val reminder = remindersDao.getReminder(reminderId)
        reminder.deleteAlarms(context)
        remindersDao.deleteReminder(reminderId)
    }

    override suspend fun deleteAllReminders() {
        remindersDao.deleteAllReminders()
    }
}