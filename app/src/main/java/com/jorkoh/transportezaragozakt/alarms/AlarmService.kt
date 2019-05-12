package com.jorkoh.transportezaragozakt.alarms

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.tasks.setupNotificationChannels
import android.os.Build
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.stop_details.createStopDetailsDeepLink
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.util.CombinedLiveData
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.repositories.util.Status
import org.koin.android.ext.android.inject


class AlarmService : LifecycleService() {

    private val stopsRepository: StopsRepository by inject()
    private lateinit var stopId: String
    private lateinit var stopType: StopType

    private val stopDestinationsObserver =
        Observer<Pair<Resource<List<StopDestination>>?, String?>> { stopInformation ->
            if (stopInformation.first != null && stopInformation.first?.status != Status.LOADING && stopInformation.second != null) {
                createNotification(
                    stopInformation.first as Resource<List<StopDestination>>,
                    stopInformation.second as String
                )
                stopSelf()
            }
        }

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

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        StopDetailsFragmentArgs.fromBundle(requireNotNull(intent.extras)).let {
            stopId = it.stopId
            stopType = StopType.valueOf(it.stopType)
        }

        //TODO: Maybe it would make more sense to use the reminder alias instead of the stop title
        val stopInformation =
            CombinedLiveData(
                stopsRepository.loadStopDestinations(stopId, stopType),
                stopsRepository.loadStopTitle(stopId)
            ) { x, y -> Pair(x, y) }

        stopInformation.observe(this, stopDestinationsObserver)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification(stopDestinations: Resource<List<StopDestination>>, stopTitle: String) {
        //NavDeepLinkBuilder doesn't work with bottom navigation view navigation so let's create the deep link normally
        val notificationIntent = createStopDetailsDeepLink(stopId, stopType)

        //TODO request code and flag?
        val pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (stopDestinations.status == Status.SUCCESS && !stopDestinations.data.isNullOrEmpty()) {
            val notificationRemoteView = RemoteViews(packageName, R.layout.notification_custom).apply{
                setTextViewText(
                    R.id.notification_stop_title,
                    stopTitle
                )
            }
            val notificationBigRemoteView = RemoteViews(packageName, R.layout.notification_custom).apply{
                setTextViewText(
                    R.id.notification_stop_title,
                    stopTitle
                )
            }

            val destinationRowLayout = when (stopType) {
                StopType.BUS -> R.layout.reminder_destination_row_bus
                StopType.TRAM -> R.layout.reminder_destination_row_tram
            }

            stopDestinations.data.forEachIndexed { index, stopDestination ->
                val destinationRemoteView = RemoteViews(packageName, destinationRowLayout)
                destinationRemoteView.setTextViewText(R.id.notification_line_text, stopDestination.line)
                destinationRemoteView.setTextViewText(
                    R.id.notification_time_text,
                    "${stopDestination.times[0]} - ${stopDestination.times[1]}"
                )
                destinationRemoteView.setTextViewText(R.id.notification_destination_text, stopDestination.destination)

                if (index < 2) {
                    notificationRemoteView.addView(R.id.reminder_destinations_container, destinationRemoteView)
                }
                notificationBigRemoteView.addView(R.id.reminder_destinations_container, destinationRemoteView)
            }

            val reminderNotification =
                NotificationCompat.Builder(this, getString(R.string.notification_channel_id_reminders))
                    .setCustomHeadsUpContentView(notificationRemoteView)    //256dp max
                    .setCustomContentView(notificationRemoteView)           //256dp max
                    .setCustomBigContentView(notificationBigRemoteView)     //no max, should be built programmatically?
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
}