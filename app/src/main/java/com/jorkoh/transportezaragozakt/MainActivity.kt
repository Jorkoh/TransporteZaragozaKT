package com.jorkoh.transportezaragozakt

import android.animation.ValueAnimator
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
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.jorkoh.transportezaragozakt.destinations.getColorFromAttr
import com.jorkoh.transportezaragozakt.destinations.setupWithNavController
import com.jorkoh.transportezaragozakt.tasks.enqueuePeriodicSetupRemindersWorker
import com.jorkoh.transportezaragozakt.tasks.enqueuePeriodicUpdateDataWorker
import com.jorkoh.transportezaragozakt.tasks.setupNotificationChannels
import com.pixplicity.generate.Rate
import daio.io.dresscode.matchDressCode
import kotlinx.android.synthetic.main.main_container.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    companion object {
        const val ACTION_SHORTCUT_PINNED = "ACTION_SHORTCUT_PINNED"
    }

    private val mainActivityVM: MainActivityViewModel by viewModel()
    private val rate: Rate by inject()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var currentNavController: LiveData<NavController>? = null
    private val shortcutPinnedReceiver = ShortcutPinnedReceiver()

    private val onDestinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        // Bottom navigation showing and hiding
        when (destination.id) {
            R.id.stopDetails, R.id.lineDetails, R.id.themePicker, R.id.webView -> hideBottomNavigation()
            else -> showBottomNavigation()
        }
        // Stop details has its own collapsing toolbar
        when (destination.id) {
            R.id.stopDetails -> main_toolbar.visibility = View.GONE
            else -> main_toolbar.visibility = View.VISIBLE
        }
        // Fab showing and hiding. Ideally the fab would just be part of that fragment layout but to have it properly react to snackbars
        // and the bottom navigation animations it has to be a direct child of the main_container coordinator layout.
        when (destination.id) {
            R.id.stopDetails -> {
                stop_details_fab.alpha = 0f
                stop_details_fab.visibility = View.VISIBLE
                stop_details_fab.animate().alpha(1f).setDuration(150).setStartDelay(200).start()
            }
            else -> {
                stop_details_fab.visibility = View.GONE
                stop_details_fab.close()
            }
        }
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
        setSupportActionBar(main_toolbar)
        if (savedInstanceState == null) {
            // Every time the app is started from scratch count up the engagement events towards displaying the rate-me flow
            rate.count()
            // WorkManager tasks are queued both on app launch and device boot to cover first install
            enqueuePeriodicUpdateDataWorker(getString(R.string.update_data_work_name))
            enqueuePeriodicSetupRemindersWorker(getString(R.string.setup_reminders_work_name))
            setupNotificationChannels(this)
            setupBottomNavigationBar(true)
        }

        // Setup snackbar feedback observers. Whenever the user adds/removes favorites/reminders from any screen they get a snackbar
        mainActivityVM.init()
        mainActivityVM.favoriteCountChange.observe(this, Observer { changeAmount ->
            when {
                changeAmount == 0 -> return@Observer
                changeAmount > 0 -> makeSnackbar(getString(R.string.added_favorite_snackbar))
                changeAmount < 0 -> makeSnackbar(getString(R.string.removed_favorite_snackbar))
            }
        })
        mainActivityVM.reminderCountChange.observe(this, Observer { changeAmount ->
            when {
                changeAmount == 0 -> return@Observer
                changeAmount > 0 -> makeSnackbar(getString(R.string.added_reminder_snackbar))
                changeAmount < 0 -> makeSnackbar(getString(R.string.removed_reminder_snackbar))
            }
        })
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
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
        val controller = bottom_navigation.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent,
            isAnimationDisabled = { !bottomNavigationShowing },
            firstLaunch = firstLaunch
        )

        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
            navController.removeOnDestinationChangedListener(onDestinationChangedListener)
            navController.addOnDestinationChangedListener(onDestinationChangedListener)
        })
        currentNavController = controller
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    override fun onBackPressed() {
        if (currentNavController?.value?.popBackStack() != true) {
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

    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
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
            else -> "Unknown"
        }
        firebaseAnalytics.setCurrentScreen(this, screenName, screenName)
    }


    /* Bottom navigation animation stuff. */
    // The idea is to hide the bottom navigation when the user enters a destination that can be accessed
    // from multiple root destinations and doesn't belong to one specifically. For example stop details can be opened from favorites, map,
    // search, notifications... It wouldn't make sense to have one open on the map stack and another on the favorites stack. It would
    // be even worse if the same stop is opened on different stacks. This is similar to how the youtube app handles the video screen
    private var currentAnimator: ValueAnimator? = null
    private var bottomNavigationShowing = true

    private fun hideBottomNavigation() {
        if (!bottomNavigationShowing) {
            // Avoid duplicating animations
            return
        }

        with(bottom_navigation) {
            bottomNavigationShowing = false
            currentAnimator?.end()
            currentAnimator = ValueAnimator.ofInt(measuredHeight, 1).apply {
                addUpdateListener { valueAnimator ->
                    updateLayoutParams {
                        height = valueAnimator.animatedValue as Int
                    }
                }
                interpolator = FastOutLinearInInterpolator()
                duration = 250
                doOnEnd {
                    if (!bottomNavigationShowing) {
                        visibility = View.INVISIBLE
                    }
                }
                start()
            }
        }
    }

    private fun showBottomNavigation() {
        if (bottomNavigationShowing) {
            // Avoid duplicating animations
            return
        }

        with(bottom_navigation) {
            bottomNavigationShowing = true
            currentAnimator?.end()
            currentAnimator = ValueAnimator.ofInt(
                measuredHeight,
                resources.getDimension(R.dimen.design_bottom_navigation_height).toInt()
            ).apply {
                addUpdateListener { valueAnimator ->
                    updateLayoutParams {
                        height = valueAnimator.animatedValue as Int
                    }
                }
                interpolator = FastOutLinearInInterpolator()
                duration = 250
                doOnStart {
                    visibility = View.VISIBLE
                }
                start()
            }
        }
    }

    inner class ShortcutPinnedReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            makeSnackbar(getString(R.string.pinned_shortcut_snackbar))
        }
    }
}
