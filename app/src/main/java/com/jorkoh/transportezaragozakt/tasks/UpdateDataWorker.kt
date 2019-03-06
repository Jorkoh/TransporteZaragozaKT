package com.jorkoh.transportezaragozakt.tasks

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.jorkoh.transportezaragozakt.R
import java.util.concurrent.TimeUnit

class UpdateDataWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    companion object {
        @JvmStatic
        fun enqueueWorker(){
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            //TODO: This should be changed to 1, TimeUnit.DAYS
            val updateDataRequest = PeriodicWorkRequestBuilder<UpdateDataWorker>(30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance().enqueue(updateDataRequest)
        }
    }

    override fun doWork(): Result {
        // Do the work here
        testWork()


        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    //TODO: Replace this with the actual work
    private fun testWork(){
        val notificationManager: NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val testRV = RemoteViews(applicationContext.packageName, R.layout.notification_custom)
        testRV.setTextViewText(R.id.notification_destination_text, "PASEO PAMPLONA")
        testRV.setTextViewText(R.id.notification_first_time_text, "2 minutes")
        testRV.setTextViewText(R.id.notification_second_time_text, "5 minutes")

        val testBigRV = RemoteViews(applicationContext.packageName, R.layout.notification_custom_big)
        testBigRV.setTextViewText(R.id.notification_destination_text, "PASEO PAMPLONA")
        testBigRV.setTextViewText(R.id.notification_first_time_text, "2 minutes")
        testBigRV.setTextViewText(R.id.notification_second_time_text, "5 minutes")
        testBigRV.setTextViewText(R.id.notification_destination_text2, "PASEO PAMPLONA 2")
        testBigRV.setTextViewText(R.id.notification_first_time_text2, "2 minutes")
        testBigRV.setTextViewText(R.id.notification_second_time_text2, "5 minutes")

        val testNotification = NotificationCompat.Builder(applicationContext, "TestingStuff")
            .setCustomHeadsUpContentView(testRV)    //256dp max
            .setCustomContentView(testRV)           //256dp max
            .setCustomBigContentView(testBigRV)     //no max, should be built programmatically?
            .setSmallIcon(R.drawable.ic_bus)
            .setContentTitle("My notification")
            .setContentText("Much longer text that cannot fit one line...")
//            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .setChannelId("TestingStuff")
//            .setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("TestingStuff", "TestingStuff", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(1, testNotification.build())
    }
}