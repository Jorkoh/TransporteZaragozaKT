package com.jorkoh.transportezaragozakt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Interpolator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.jorkoh.transportezaragozakt.destinations.setupWithNavController
import com.jorkoh.transportezaragozakt.tasks.enqueueWorker
import com.jorkoh.transportezaragozakt.tasks.setupNotificationChannels
import daio.io.dresscode.matchDressCode
import kotlinx.android.synthetic.main.main_container.*
import kotlinx.android.synthetic.main.stop_details_destination.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val mainActivityVM: MainActivityViewModel by viewModel()

    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        matchDressCode()
        super.onCreate(savedInstanceState)
        enqueueWorker()
        setupNotificationChannels(this)
        setContentView(R.layout.main_container)
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        val navGraphIds = listOf(
            R.navigation.favorites_destination,
            R.navigation.map_destination,
            R.navigation.search_destination,
            R.navigation.reminders_destination,
            R.navigation.more_destination
        )

        val controller = bottom_navigation.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent
        )

        setSupportActionBar(toolbar)

        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.stopDetails -> {
                        with(bottom_navigation) {
                            if (visibility == View.VISIBLE && alpha == 1f) {
                                animate()
                                    .translationY(resources.getDimension(R.dimen.design_bottom_navigation_height))
                                    .withEndAction { visibility = View.GONE }
                                    .setInterpolator(FastOutLinearInInterpolator())
                                    .duration = 150
                            }
                        }
                    }
                    else -> {
                        with(bottom_navigation) {
                            visibility = View.VISIBLE
                            animate()
                                .translationY(0f)
                                .setInterpolator(LinearOutSlowInInterpolator())
                                .duration = 150
                        }
                    }
                }
            }
        })
        currentNavController = controller
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    /**
     * Overriding popBackStack is necessary in this case if the app is started from the deep link.
     */
    override fun onBackPressed() {
        if (currentNavController?.value?.popBackStack() != true) {
            super.onBackPressed()
        }
    }

    //@TEST
    fun testNotifications(@Suppress("UNUSED_PARAMETER") v: View) {
//        val pendingIntent = NavDeepLinkBuilder(context)
//            .setGraph(R.navigation.favorites_destination)
//            .setDestination(R.id.stopDetails)
//            .setArguments(
//                StopDetailsFragmentArgs(
//                    stopDetailsVM.stopID,
//                    stopDetailsVM.stopType.name
//                ).toBundle()
//            ).createPendingIntent()
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
            .setChannelId("TestingStuff")
//            .setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("TestingStuff", "TestingStuff", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(1, testNotification.build())
    }

    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }
}
