package com.jorkoh.transportezaragozakt.tasks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jorkoh.transportezaragozakt.R


class SystemActionsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                setupNotificationChannels(context)
                enqueuePeriodicUpdateDataWorker(context.getString(R.string.update_data_work_name))
                enqueuePeriodicSetupRemindersWorker(context.getString(R.string.setup_reminders_work_name))
            }
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                enqueueOneTimeSetupRemindersWorker()
            }
        }
    }
}