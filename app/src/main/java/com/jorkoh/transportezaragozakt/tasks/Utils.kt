package com.jorkoh.transportezaragozakt.tasks

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.*
import com.jorkoh.transportezaragozakt.R
import java.util.concurrent.TimeUnit

//https://stackoverflow.com/questions/53043183/how-to-register-a-periodic-work-request-with-workmanger-system-wide-once-i-e-a
fun enqueueUpdateDataWorker(workName: String) {
    val updateDataRequest = PeriodicWorkRequestBuilder<UpdateDataWorker>(1, TimeUnit.DAYS)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

    WorkManager.getInstance().enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.KEEP, updateDataRequest)
}

fun enqueueSetupRemindersWorker(workName: String) {
    val updateDataRequest = PeriodicWorkRequestBuilder<SetupRemindersWorker>(12, TimeUnit.HOURS)
        .setConstraints(
            Constraints.Builder()
                .build()
        )
        .build()

    WorkManager.getInstance().enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.KEEP, updateDataRequest)
}

fun setupNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val reminderChannel = NotificationChannel(
            context.getString(R.string.notification_channel_id_reminders),
            context.getString(R.string.notification_channel_name_reminders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_description_reminders)
        }

        val foregroundChannel = NotificationChannel(
            context.getString(R.string.notification_channel_id_services),
            context.getString(R.string.notification_channel_name_services),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_description_services)
        }
        notificationManager.createNotificationChannel(reminderChannel)
        notificationManager.createNotificationChannel(foregroundChannel)
    }
}