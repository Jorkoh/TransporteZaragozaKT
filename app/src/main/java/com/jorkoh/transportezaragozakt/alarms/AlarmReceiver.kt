package com.jorkoh.transportezaragozakt.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    // When a reminder alarm pops start a foreground service to handle the data fetching and notification creation.
    // We could probably get away with doing it directly on the broadcast receiver but if real-time notification updating
    // is implemented in the future a foreground service will be necessary.
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