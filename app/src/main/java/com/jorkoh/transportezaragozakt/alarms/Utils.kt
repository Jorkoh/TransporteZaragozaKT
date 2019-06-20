package com.jorkoh.transportezaragozakt.alarms

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Bundle
import com.jorkoh.transportezaragozakt.db.DaysOfWeek
import com.jorkoh.transportezaragozakt.db.Reminder
import java.text.SimpleDateFormat
import java.util.*

// Alarms are created (or recreated) when a reminder is created, updated or when the setup reminders periodic task fires.
// The periodic task requeuing combined with setting alarms some days in advance should ensure that they keep firing
fun Reminder.createAlarms(context: Context, daysInAdvance: Int = 7) {
    val currentTime = Calendar.getInstance()
    val reminderTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, minute)
    }

    repeat(daysInAdvance) {
        // Avoid creating the alarm if the time has already passed. For example if the user
        // sets a reminder for the next morning on the afternoon
        if (reminderTime > currentTime && isEnabledThisDay(daysOfWeek, reminderTime)) {
            createAlarm(context, reminderTime)
        }
        reminderTime.add(Calendar.DATE, 1)
    }
}

const val STOP_ID_KEY_ALARM = "STOP_ID_KEY_ALARM"
const val STOP_TYPE_KEY_ALARM = "STOP_TYPE_KEY_ALARM"
const val REMINDER_ID_KEY_ALARM = "REMINDER_ID_KEY_ALARM"
private fun Reminder.createAlarm(context: Context, reminderTime: Calendar) {
    val notifyIntent = Intent(context, AlarmReceiver::class.java)
    notifyIntent.putExtras(
        Bundle().apply {
            putString(STOP_TYPE_KEY_ALARM, type.name)
            putString(STOP_ID_KEY_ALARM, stopId)
            putInt(REMINDER_ID_KEY_ALARM, reminderId)
        }
    )
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        createRequestCode(reminderId, reminderTime),
        notifyIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
    //https://stackoverflow.com/questions/51343550/how-to-give-notifications-on-android-on-specific-time-in-android-oreo/51376922#51376922
    alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(reminderTime.timeInMillis, pendingIntent), pendingIntent)
}

fun Reminder.deleteAlarms(context: Context, daysInAdvance: Int = 7) {
    val notifyIntent = Intent(context, AlarmReceiver::class.java)
    notifyIntent.putExtras(
        Bundle().apply {
            putString(STOP_TYPE_KEY_ALARM, type.name)
            putString(STOP_ID_KEY_ALARM, stopId)
            putInt(REMINDER_ID_KEY_ALARM, reminderId)
        }
    )

    val reminderTime = Calendar.getInstance()
    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

    repeat(daysInAdvance) {
        // No need to check whether some of this days are enabled or not, if there is no alarm setup
        // the cancel Intent will simply be ignored
        reminderTime.add(Calendar.DATE, 1)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            createRequestCode(reminderId, reminderTime),
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)
    }
}

@SuppressLint("SimpleDateFormat")
private fun createRequestCode(reminderId: Int, reminderTime: Calendar): Int {
    // This is a bit sketchy, puts a theoretical hard limit of 9999 reminders created during
    // the app install lifespan, months go first to get more out of Int range
    val datePart = SimpleDateFormat("MMyydd").format(reminderTime.time)
    return "$datePart$reminderId".toInt()
}

private fun isEnabledThisDay(daysOfWeek: DaysOfWeek, reminderTime: Calendar): Boolean {
    return when (reminderTime.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> daysOfWeek.days[0]
        Calendar.TUESDAY -> daysOfWeek.days[1]
        Calendar.WEDNESDAY -> daysOfWeek.days[2]
        Calendar.THURSDAY -> daysOfWeek.days[3]
        Calendar.FRIDAY -> daysOfWeek.days[4]
        Calendar.SATURDAY -> daysOfWeek.days[5]
        Calendar.SUNDAY -> daysOfWeek.days[6]
        else -> false
    }
}