package com.jorkoh.transportezaragozakt.tasks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class SystemActionsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                setupNotificationChannels(context)
                enqueuePeriodicUpdateDataWorker(context)
                enqueuePeriodicSetupRemindersWorker(context)
            }
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                enqueueOneTimeSetupRemindersWorker(context)
            }
        }
    }
}