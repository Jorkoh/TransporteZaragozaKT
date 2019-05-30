package com.jorkoh.transportezaragozakt.alarms

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.stop_details.createStopDetailsDeepLink
import com.jorkoh.transportezaragozakt.repositories.RemindersRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.util.CombinedLiveData
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.repositories.util.Status
import com.jorkoh.transportezaragozakt.tasks.setupNotificationChannels
import org.koin.android.ext.android.inject


class AlarmService : LifecycleService() {

    companion object {
        const val STOP_ID_KEY_ALARM = "STOP_ID_KEY_ALARM"
        const val STOP_TYPE_KEY_ALARM = "STOP_TYPE_KEY_ALARM"
        const val REMINDER_ID_KEY_ALARM = "REMINDER_ID_KEY_ALARM"
    }

    private val stopsRepository: StopsRepository by inject()
    private val remindersRepository: RemindersRepository by inject()
    private lateinit var stopId: String
    private lateinit var stopType: StopType
    private var reminderId: Int = 1


    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            setupNotificationChannels(this)
            val foregroundServiceNotification =
                NotificationCompat.Builder(this, getString(R.string.notification_channel_id_services))
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.notification_content_services_reminders))
                    .build()
            // -1 As the notification id of the service avoids conflict with the reminder notifications
            startForeground(-1, foregroundServiceNotification)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            requireNotNull(intent.extras).run {
                stopId = requireNotNull(getString(STOP_ID_KEY_ALARM))
                stopType = StopType.valueOf(requireNotNull(getString(STOP_TYPE_KEY_ALARM)))
                reminderId = getInt(REMINDER_ID_KEY_ALARM)
            }
        } catch (e: IllegalArgumentException) {
            stopSelf()
        }

        CombinedLiveData(
            stopsRepository.loadStopDestinations(stopId, stopType),
            remindersRepository.loadReminderAlias(reminderId)
        ) { x, y -> Pair(x, y) }.observe(this, Observer { info ->
            if (info.first?.status != Status.LOADING) {
                createNotification(
                    info.first as Resource<List<StopDestination>>,
                    info.second as String
                )
                stopSelf()
            }
        })

        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

    private fun createNotification(stopDestinations: Resource<List<StopDestination>>, reminderAlias: String) {
        //NavDeepLinkBuilder doesn't work with bottom navigation view navigation so let's create the deep link normally
        val pendingIntent = PendingIntent.getActivity(
            this,
            reminderId,
            createStopDetailsDeepLink(stopId, stopType),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val reminderNotification =
            NotificationCompat.Builder(this, getString(R.string.notification_channel_id_reminders)).apply {
                if (stopDestinations.status == Status.SUCCESS && !stopDestinations.data.isNullOrEmpty()) {
                    val notificationRemoteViews = createRemoteViews(stopDestinations, reminderAlias)
                    setCustomHeadsUpContentView(notificationRemoteViews.contentRemoteView)    //256dp max
                    setCustomContentView(notificationRemoteViews.contentRemoteView)           //256dp max
                    setCustomBigContentView(notificationRemoteViews.bigContentRemoteView)     //no max, should be built programmatically?
                    setContentTitle("")
                    setContentText("")
                } else {
                    setContentTitle(reminderAlias)
                    setContentText(getString(R.string.notification_error))
                }
                setSmallIcon(R.drawable.ic_notification_icon)
                priority = NotificationCompat.PRIORITY_HIGH
                setAutoCancel(true)
                setDefaults(NotificationCompat.DEFAULT_SOUND)
                setContentIntent(pendingIntent)
                setChannelId(getString(R.string.notification_channel_id_reminders))
            }
        setupNotificationChannels(this)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(reminderId, reminderNotification.build())
    }

    private fun createRemoteViews(
        stopDestinations: Resource<List<StopDestination>>,
        reminderAlias: String
    ): NotificationRemoteViews {
        val remoteView = RemoteViews(packageName, R.layout.notification_custom).apply {
            setTextViewText(R.id.notification_stop_title, reminderAlias)
        }
        val bigRemoteView = RemoteViews(packageName, R.layout.notification_custom).apply {
            setTextViewText(R.id.notification_stop_title, reminderAlias)
        }

        val destinationRowLayout = when (stopType) {
            StopType.BUS -> R.layout.notification_destination_row_bus
            StopType.TRAM -> R.layout.notification_destination_row_tram
        }

        stopDestinations.data?.forEachIndexed { index, stopDestination ->
            val destinationRemoteView = RemoteViews(packageName, destinationRowLayout)
            destinationRemoteView.setTextViewText(R.id.notification_line_text, stopDestination.line)
            destinationRemoteView.setTextViewText(
                R.id.notification_time_text,
                "${stopDestination.times[0]} - ${stopDestination.times[1]}"
            )
            destinationRemoteView.setTextViewText(R.id.notification_destination_text, stopDestination.destination)

            if (index < 2) {
                remoteView.addView(R.id.reminder_destinations_container, destinationRemoteView)
            }
            bigRemoteView.addView(R.id.reminder_destinations_container, destinationRemoteView)
        }
        return NotificationRemoteViews(remoteView, bigRemoteView)
    }

    data class NotificationRemoteViews(val contentRemoteView: RemoteViews?, val bigContentRemoteView: RemoteViews?)
}