package com.jorkoh.transportezaragozakt.tasks

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.alarms.createAlarms
import com.jorkoh.transportezaragozakt.db.RemindersDao
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.db.StopsDao
import com.parse.ParseObject
import com.parse.ParseQuery
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
        Log.d("TestingStuff", "Setup Reminders worker fired!")

        remindersDao.getRemindersImmediate().forEach {reminder ->
            reminder.createAlarms(applicationContext)
        }

        return Result.success()
    }
}