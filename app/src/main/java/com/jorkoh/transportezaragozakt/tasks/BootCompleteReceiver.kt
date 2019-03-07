package com.jorkoh.transportezaragozakt.tasks

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build


class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            //TODO: clean up the notification stuff
            setupNotificationChannels(context)
            UpdateDataWorker.enqueueWorker()
        }
    }

    private fun setupNotificationChannels(context: Context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("TestingStuff", "TestingStuff", NotificationManager.IMPORTANCE_HIGH)
            val notificationManager =context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}