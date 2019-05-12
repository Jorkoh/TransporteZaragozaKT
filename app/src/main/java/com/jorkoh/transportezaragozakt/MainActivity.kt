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
import androidx.navigation.NavDestination
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

    private val onDestinationChangedListener = NavController.OnDestinationChangedListener{ _, destination, _ ->
        Log.d("TESTING STUFF", "DESTINATION CHANGED LISTENER ${destination.id}")
        when (destination.id) {
            R.id.stopDetails -> hideBottomNavigation()
            else -> showBottomNavigation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        matchDressCode()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_container)
        if (savedInstanceState == null) {
            enqueueWorker()
            setupNotificationChannels(this)
            setupBottomNavigationBar()
        }else{
//            showSearchBar()
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
            Log.d("TESTING STUFF", "NAV CONTROLLER CHANGED ${navController.currentDestination.toString()}")
            when(navController.graph.startDestination){
                R.id.search -> showSearchBar()
                else -> hideSearchBar()
            }
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
            Log.d("TESTING STUFF", "SUPER ON BACK PRESSED")
            super.onBackPressed()
        }
    }

    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    private fun hideBottomNavigation() {
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

    private fun showBottomNavigation() {
        with(bottom_navigation) {
            visibility = View.VISIBLE
            animate()
                .translationY(0f)
                .setInterpolator(LinearOutSlowInInterpolator())
                .duration = 150
        }
    }

    private fun showSearchBar() {
        Log.d("TESTING STUFF", "SHOWING SEARCH BAR")
        toolbar.menu.clear()
        menuInflater.inflate(R.menu.search_destination_menu, toolbar.menu)
    }

    private fun hideSearchBar() {
        Log.d("TESTING STUFF", "HIDING SEARCH BAR")
        toolbar.menu.clear()
    }
}
