package com.jorkoh.transportezaragozakt.tasks

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jorkoh.transportezaragozakt.alarms.createAlarms
import com.jorkoh.transportezaragozakt.db.daos.RemindersDao
import org.koin.core.KoinComponent
import org.koin.core.inject

class SetupRemindersWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams),
    KoinComponent {

    private val remindersDao: RemindersDao by inject()

    override fun doWork(): Result {
        // May fire multiple times?
        // https://issuetracker.google.com/issues/119886476
        remindersDao.getRemindersImmediate().forEach {reminder ->
            reminder.createAlarms(applicationContext)
        }

        return Result.success()
    }
}