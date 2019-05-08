package com.jorkoh.transportezaragozakt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Interpolator
import android.os.Build
import android.os.Bundle
import android.util.Log
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

    override fun onBackPressed() {
        if (currentNavController?.value?.popBackStack() != true) {
            super.onBackPressed()
        }
    }

    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }
}
