package com.jorkoh.transportezaragozakt.alarms

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.tasks.setupNotificationChannels
import android.os.Build
import android.net.Uri


class AlarmService : IntentService("AlarmService") {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            setupNotificationChannels(this)
            val foregroundServiceNotification =
                NotificationCompat.Builder(this, getString(R.string.notification_channel_id_services))
                    .setSmallIcon(R.mipmap.ic_bus_launcher_round)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.notification_content_services_reminders))
                    .build()
            startForeground(1, foregroundServiceNotification)
        }
    }

    override fun onHandleIntent(intent: Intent) {
        //IF THERE ARE MORE THAN 2(?) destinations use setCustomBigContentView()

        val stopDetailsFragmentArgs = StopDetailsFragmentArgs.fromBundle(requireNotNull(intent.extras))

        //NavDeepLinkBuilder doesn't work with bottom navigation view navigation so let's create the deep link normally
        //TODO Creating this URIs should be an utility method
        val notificationIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("launchTZ://viewStop/${stopDetailsFragmentArgs.stopType}/${stopDetailsFragmentArgs.stopId}/")
        )
        //TODO request code and flag?
        val pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val destinationRemoteView = RemoteViews(packageName, R.layout.reminder_destination_row)
        destinationRemoteView.setTextViewText(R.id.notification_line_text, "23")
        destinationRemoteView.setTextViewText(R.id.notification_time_text, "2 minutes - 5 minutes")
        destinationRemoteView.setTextViewText(R.id.notification_destination_text, "PASEO PAMPLONA")

        val destinationRemoteView2 = RemoteViews(packageName, R.layout.reminder_destination_row)
        destinationRemoteView2.setTextViewText(R.id.notification_line_text, "42")
        destinationRemoteView2.setTextViewText(R.id.notification_time_text, "3 minutes - 10 minutes")
        destinationRemoteView2.setTextViewText(R.id.notification_destination_text, "PASEO PAMPLONA")

        val notificationRemoteView = RemoteViews(packageName, R.layout.notification_custom)
        notificationRemoteView.addView(R.id.reminder_destinations_container, destinationRemoteView)
        notificationRemoteView.addView(R.id.reminder_destinations_container, destinationRemoteView2)

        val testBigRV = RemoteViews(packageName, R.layout.notification_custom_big)
        testBigRV.setTextViewText(R.id.notification_destination_text, "PASEO PAMPLONA")
        testBigRV.setTextViewText(R.id.notification_first_time_text, "2 minutes")
        testBigRV.setTextViewText(R.id.notification_second_time_text, "5 minutes")
        testBigRV.setTextViewText(R.id.notification_destination_text2, "PASEO PAMPLONA 2")
        testBigRV.setTextViewText(R.id.notification_first_time_text2, "2 minutes")
        testBigRV.setTextViewText(R.id.notification_second_time_text2, "5 minutes")

        val reminderNotification =
            NotificationCompat.Builder(this, getString(R.string.notification_channel_id_reminders))
                .setCustomHeadsUpContentView(notificationRemoteView)    //256dp max
                .setCustomContentView(notificationRemoteView)           //256dp max
                .setCustomBigContentView(notificationRemoteView)     //no max, should be built programmatically?
                .setSmallIcon(R.mipmap.ic_bus_launcher_round)
                .setContentTitle("My notification")
                .setContentText("Much longer text that cannot fit one line...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setChannelId(getString(R.string.notification_channel_id_reminders))

        setupNotificationChannels(this)

        notificationManager.notify(50, reminderNotification.build())
    }

}