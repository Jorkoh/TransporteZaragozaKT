package com.jorkoh.transportezaragozakt.tasks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            UpdateDataWorker.enqueueWorker()
        }
    }
}