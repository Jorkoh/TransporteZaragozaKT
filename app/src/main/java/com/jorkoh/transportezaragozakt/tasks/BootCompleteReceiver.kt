package com.jorkoh.transportezaragozakt.tasks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jorkoh.transportezaragozakt.R


class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            setupNotificationChannels(context)
            enqueueUpdateDataWorker(context.getString(R.string.update_data_work_name))
            enqueueSetupRemindersWorker(context.getString(R.string.setup_reminders_work_name))
        }
    }
}