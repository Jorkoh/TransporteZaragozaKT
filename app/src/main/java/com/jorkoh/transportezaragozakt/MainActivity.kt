package com.jorkoh.transportezaragozakt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.jorkoh.transportezaragozakt.destinations.utils.*
import com.jorkoh.transportezaragozakt.tasks.enqueuePeriodicSetupRemindersWorker
import com.jorkoh.transportezaragozakt.tasks.enqueuePeriodicUpdateDataWorker
import com.jorkoh.transportezaragozakt.tasks.setupNotificationChannels
import com.pixplicity.generate.Rate
import daio.io.dresscode.matchDressCode
import kotlinx.android.synthetic.main.main_container.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    companion object {
        const val ACTION_SHORTCUT_PINNED = "ACTION_SHORTCUT_PINNED"

        val DESTINATIONS_WITH_BOTTOM_NAVIGATION = listOf(R.id.favorites, R.id.map, R.id.search, R.id.reminders, R.id.more)
    }

    private val mainActivityVM: MainActivityViewModel by viewModel()
    private val rate: Rate by inject()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val shortcutPinnedReceiver = ShortcutPinnedReceiver()

    private val onDestinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        // Fab showing and hiding, the Fab is part of the root coordinator layout instead of the specific fragment layout.
        animateStopDetailsFab(destination.id)
        // Bottom navigation showing and hiding
        animateBottomNavigation(destination.id)

        logDestinationVisit(destination.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        matchDressCode()
        super.onCreate(savedInstanceState)

        // Start the intro if needed
        if (savedInstanceState == null && mainActivityVM.isFirstLaunch()) {
            startActivity(Intent(this, IntroActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
        }

        setContentView(R.layout.main_container)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        if (savedInstanceState == null) {
            // Every time the app is started from scratch count up the engagement events towards displaying the rate-me flow
            rate.count()
            // WorkManager tasks are queued both on app launch and device boot to cover first install
            enqueuePeriodicUpdateDataWorker(this)
            enqueuePeriodicSetupRemindersWorker(this)
            setupNotificationChannels(this)
            setupBottomNavigationBar(true)
        }

        // Setup snackbar feedback observers. Whenever the user adds/removes favorites/reminders from any screen they get a snackbar
        lifecycleScope.launch {
            mainActivityVM.favoriteCountChangeSign.collect { changeSign ->
                when (changeSign) {
                    true -> makeSnackbar(getString(R.string.added_favorite_snackbar))
                    false -> makeSnackbar(getString(R.string.removed_favorite_snackbar))
                }
            }
        }
        lifecycleScope.launch {
            mainActivityVM.reminderCountChangeSign.collect { changeSign ->
                when (changeSign) {
                    true -> makeSnackbar(getString(R.string.added_reminder_snackbar))
                    false -> makeSnackbar(getString(R.string.removed_reminder_snackbar))
                }
            }
        }
        mainActivityVM.currentNavController.observe(this, Observer { navController ->
            navController.removeOnDestinationChangedListener(onDestinationChangedListener)
            navController.addOnDestinationChangedListener(onDestinationChangedListener)
        })
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar(false)
    }

    private fun setupBottomNavigationBar(firstLaunch: Boolean) {
        val navGraphIds = listOf(
            R.navigation.favorites_destination,
            R.navigation.map_destination,
            R.navigation.search_destination,
            R.navigation.reminders_destination,
            R.navigation.more_destination
        )

        // "bottomNavigationShowing" is passed to avoid users with quick fingers navigating into another destination while the
        // bottom navigation is in the process of hiding. Easier and cleaner than trying to directly disable clicks on it
        val newController = bottom_navigation.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent,
            isAnimationDisabled = { !bottomNavigationShowing },
            firstLaunch = firstLaunch
        )
        mainActivityVM.setController(newController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return mainActivityVM.currentNavController.value?.navigateUp() ?: false
    }

    override fun onBackPressed() {
        if (mainActivityVM.currentNavController.value?.popBackStack() != true) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        // Broadcast receiver for shortcut pinning callback
        registerReceiver(shortcutPinnedReceiver, IntentFilter(ACTION_SHORTCUT_PINNED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(shortcutPinnedReceiver)
    }

    fun makeSnackbar(text: String) {
        Snackbar.make(
            coordinator_layout,
            text,
            Snackbar.LENGTH_LONG
        ).apply {
            view.layoutParams = (view.layoutParams as CoordinatorLayout.LayoutParams).apply {
                anchorId = R.id.gap
                anchorGravity = Gravity.TOP
                gravity = Gravity.TOP
            }
            view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                .setTextColor(getColorFromAttr(R.attr.colorOnSnackbar))
        }.show()
    }

    //Log the destinations in Firebase, it only supports activities by default
    private fun logDestinationVisit(destinationId: Int) {
        val screenName = when (destinationId) {
            R.id.favorites -> "Favorites"
            R.id.map -> "Map"
            R.id.search -> "Search"
            R.id.reminders -> "Reminders"
            R.id.more -> "More"
            R.id.stopDetails -> "StopDetails"
            R.id.lineDetails -> "LineDetails"
            R.id.themePicker -> "ThemePicker"
            R.id.webView -> "WebView"
            else -> "Unknown"
        }
        firebaseAnalytics.setCurrentScreen(this, screenName, screenName)
    }


    /* Bottom navigation animation stuff. */
    // The idea is to hide the bottom navigation when the user enters a destination that can be accessed
    // from multiple root destinations and doesn't belong to one specifically. For example stop details can be opened from favorites, map,
    // search, notifications... It wouldn't make sense to have one open on the map stack and another on the favorites stack. It would
    // be even worse if the same stop is opened on different stacks. This is similar to how the youtube app handles the video screen
    private var bottomNavigationShowing = true

    private fun animateBottomNavigation(destinationId: Int) {
        if (DESTINATIONS_WITH_BOTTOM_NAVIGATION.contains(destinationId) && !bottomNavigationShowing) {
            bottom_navigation.slideUpToShow()
            bottomNavigationShowing = true
        } else if (!DESTINATIONS_WITH_BOTTOM_NAVIGATION.contains(destinationId) && bottomNavigationShowing) {
            bottom_navigation.slideDownToHide()
            bottomNavigationShowing = false
        }
    }

    private fun animateStopDetailsFab(destinationId: Int) {
        if (destinationId == R.id.stopDetails && stop_details_fab.visibility != View.VISIBLE) {
            TransitionManager.beginDelayedTransition(
                coordinator_layout,
                Slide(Gravity.END).apply {
                    duration = ANIMATE_INTO_DETAILS_SCREEN_DURATION
                    interpolator = LINEAR_OUT_SLOW_IN
                    mode = Slide.MODE_IN
                    addTarget(R.id.stop_details_fab)
                }
            )
            stop_details_fab.show()
        } else if (destinationId != R.id.stopDetails && stop_details_fab.visibility == View.VISIBLE) {
            TransitionManager.beginDelayedTransition(
                coordinator_layout,
                Slide(Gravity.END).apply {
                    duration = ANIMATE_OUT_OF_DETAILS_SCREEN_DURATION
                    interpolator = FAST_OUT_LINEAR_IN
                    mode = Slide.MODE_OUT
                    addTarget(R.id.stop_details_fab)
                }
            )
            stop_details_fab.close()
            stop_details_fab.visibility = View.GONE
        }
    }


    inner class ShortcutPinnedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            makeSnackbar(getString(R.string.pinned_shortcut_snackbar))
        }
    }
}
