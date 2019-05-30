package com.jorkoh.transportezaragozakt.tasks

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.alarms.createAlarms
import com.jorkoh.transportezaragozakt.db.RemindersDao
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject


class SetupRemindersWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams),
    KoinComponent {

    private val remindersDao: RemindersDao by inject()
    private val executors: AppExecutors by inject()
    private val sharedPreferences : SharedPreferences by inject()

    override fun doWork(): Result {
        // May fire multiple times?
        // https://issuetracker.google.com/issues/119886476
        remindersDao.getRemindersImmediate().forEach {reminder ->
            reminder.createAlarms(applicationContext)
        }

        return Result.success()
    }
}