package com.jorkoh.transportezaragozakt.alarms

import android.app.IntentService
import android.app.NotificationManager
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.tasks.setupNotificationChannels

class AlarmService : IntentService("AlarmService") {
    companion object {

    }

    override fun onHandleIntent(intent: Intent) {
        val stopDetailsFragmentArgs = StopDetailsFragmentArgs.fromBundle(requireNotNull(intent.extras))

        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.favorites_destination)
            .setDestination(R.id.stopDetails)
            .setArguments(stopDetailsFragmentArgs.toBundle())
            .createPendingIntent()

        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val testRV = RemoteViews(packageName, R.layout.notification_custom)
        testRV.setTextViewText(R.id.notification_destination_text, "PASEO PAMPLONA")
        testRV.setTextViewText(R.id.notification_first_time_text, "2 minutes")
        testRV.setTextViewText(R.id.notification_second_time_text, "5 minutes")

        val testBigRV = RemoteViews(packageName, R.layout.notification_custom_big)
        testBigRV.setTextViewText(R.id.notification_destination_text, "PASEO PAMPLONA")
        testBigRV.setTextViewText(R.id.notification_first_time_text, "2 minutes")
        testBigRV.setTextViewText(R.id.notification_second_time_text, "5 minutes")
        testBigRV.setTextViewText(R.id.notification_destination_text2, "PASEO PAMPLONA 2")
        testBigRV.setTextViewText(R.id.notification_first_time_text2, "2 minutes")
        testBigRV.setTextViewText(R.id.notification_second_time_text2, "5 minutes")

        val testNotification = NotificationCompat.Builder(this, "TestingStuff")
            .setCustomHeadsUpContentView(testRV)    //256dp max
            .setCustomContentView(testRV)           //256dp max
            .setCustomBigContentView(testBigRV)     //no max, should be built programmatically?
            .setSmallIcon(R.drawable.ic_bus)
            .setContentTitle("My notification")
            .setContentText("Much longer text that cannot fit one line...")
//            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .setContentIntent(pendingIntent)
            .setChannelId(getString(R.string.notification_channel_id_reminders))
//            .setOngoing(true)

        setupNotificationChannels(this)

        Log.d("TESTING STUFF", "Notification should display")
        notificationManager.notify(1, testNotification.build())
    }

}