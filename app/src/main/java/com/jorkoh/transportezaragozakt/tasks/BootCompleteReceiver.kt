package com.jorkoh.transportezaragozakt.tasks

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.jorkoh.transportezaragozakt.R


class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            //TODO: clean up the notification stuff
            setupNotificationChannels(context)
            enqueueWorker()
        }
    }
}