package com.jorkoh.transportezaragozakt.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val alarmServiceIntent = Intent(context, AlarmService::class.java)
            intent.extras?.let { extras ->
                alarmServiceIntent.putExtras(extras)
            }
            ContextCompat.startForegroundService(context, alarmServiceIntent)
        }
    }

}