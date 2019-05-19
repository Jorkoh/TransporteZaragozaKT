package com.jorkoh.transportezaragozakt

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.R.attr.start
import android.view.ViewGroup
import android.animation.ValueAnimator
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private val mainActivityVM: MainActivityViewModel by viewModel()

    private var currentNavController: LiveData<NavController>? = null

    private val onDestinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when (destination.id) {
            R.id.stopDetails -> hideBottomNavigation()
            R.id.lineDetails -> hideBottomNavigation()
            else -> showBottomNavigation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        matchDressCode()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_container)
        setSupportActionBar(main_toolbar)
        if (savedInstanceState == null) {
            enqueueWorker(getString(R.string.update_data_work_name))
            setupNotificationChannels(this)
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
            intent = intent,
            isAnimationDisabled = { !showing }
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

    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    private var currentAnimator: ValueAnimator? = null
    private var showing = true

    private fun hideBottomNavigation() {
        if(!showing){
            //Avoid duplicating animations
            return
        }
        with(bottom_navigation) {
            showing = false
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
                    if (!showing) {
                        visibility = View.GONE
                    }
                }
                start()
            }
        }
    }

    private fun showBottomNavigation() {
        if(showing){
            //Avoid duplicating animations
            return
        }
        with(bottom_navigation) {
            showing = true
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
}
