package com.jorkoh.transportezaragozakt.alarms

import com.jorkoh.transportezaragozakt.db.Reminder
import android.app.AlarmManager
import android.content.Context.ALARM_SERVICE
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import java.util.*


fun Reminder.createAlarm(context: Context) {
    val notifyIntent = Intent(context, AlarmReceiver::class.java)
    notifyIntent.putExtras(
        StopDetailsFragmentArgs(
            type.name,
            stopId
        ).toBundle())
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminderId,
        notifyIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val currentTime = Calendar.getInstance()
    val reminderTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, minute)
    }
    if (reminderTime.timeInMillis < currentTime.timeInMillis) {
        reminderTime.add(Calendar.DATE, 1)
    }

    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
    //https://stackoverflow.com/questions/51343550/how-to-give-notifications-on-android-on-specific-time-in-android-oreo/51376922#51376922
    alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(reminderTime.timeInMillis, pendingIntent), pendingIntent)
}