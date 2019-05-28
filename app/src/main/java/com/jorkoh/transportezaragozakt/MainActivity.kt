package com.jorkoh.transportezaragozakt

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.jorkoh.transportezaragozakt.destinations.setupWithNavController
import com.jorkoh.transportezaragozakt.tasks.enqueueUpdateDataWorker
import com.jorkoh.transportezaragozakt.tasks.setupNotificationChannels
import daio.io.dresscode.matchDressCode
import kotlinx.android.synthetic.main.main_container.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.updateLayoutParams
import com.google.android.material.snackbar.Snackbar
import com.jorkoh.transportezaragozakt.destinations.line_details.toPx
import com.jorkoh.transportezaragozakt.destinations.more.ThemePickerFragment
import com.jorkoh.transportezaragozakt.destinations.stop_details.getColorFromAttr
import com.jorkoh.transportezaragozakt.tasks.enqueueSetupRemindersWorker
import kotlinx.android.synthetic.main.favorites_destination.*
import kotlinx.android.synthetic.main.more_destination.*


class MainActivity : AppCompatActivity() {

    private val mainActivityVM: MainActivityViewModel by viewModel()

    private var currentNavController: LiveData<NavController>? = null
    private var currentAnimator: ValueAnimator? = null
    private var showing = true

    private val onDestinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when (destination.id) {
            R.id.stopDetails -> hideBottomNavigation()
            R.id.lineDetails -> hideBottomNavigation()
            else -> showBottomNavigation()
        }
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
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        matchDressCode()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_container)
        setSupportActionBar(main_toolbar)
        if (savedInstanceState == null) {
            //WorkManager tasks are queued both on app launch and device boot to cover first install
            enqueueUpdateDataWorker(getString(R.string.update_data_work_name))
            enqueueSetupRemindersWorker(getString(R.string.setup_reminders_work_name))
            setupNotificationChannels(this)
            setupBottomNavigationBar(true)
        }

        mainActivityVM.init()
        mainActivityVM.favoriteCountChange.observe(this, Observer { changeAmount ->
            when {
                changeAmount == 0 -> return@Observer
                changeAmount > 0 -> makeSnackbar(getString(R.string.added_favorite_snackbar))
                else -> makeSnackbar(getString(R.string.removed_favorite_snackbar))
            }
        })
        mainActivityVM.reminderCountChange.observe(this, Observer { changeAmount ->
            when {
                changeAmount == 0 -> return@Observer
                changeAmount > 0 -> makeSnackbar(getString(R.string.added_reminder_snackbar))
                else -> makeSnackbar(getString(R.string.removed_reminder_snackbar))
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


        val controller = bottom_navigation.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent,
            isAnimationDisabled = { !showing },
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

    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    private fun hideBottomNavigation() {
        if (!showing) {
            //Avoid duplicating animations
            return
        }

        nav_host_container.updateLayoutParams<CoordinatorLayout.LayoutParams> { bottomMargin = 0 }
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
        if (showing) {
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
                doOnEnd {
                    if (showing) {
                        nav_host_container.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                            bottomMargin = resources.getDimension(R.dimen.design_bottom_navigation_height).toInt()
                        }
                    }
                }
                start()
            }
        }
    }

    fun makeSnackbar(text: String) {
        Snackbar.make(
            coordinator_layout,
            text,
            Snackbar.LENGTH_LONG
        ).apply {
            view.layoutParams = (view.layoutParams as CoordinatorLayout.LayoutParams).apply {
                anchorId = R.id.bottom_navigation
                anchorGravity = Gravity.TOP
                gravity = Gravity.TOP
            }
            view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                .setTextColor(getColorFromAttr(R.attr.colorOnSnackbar))
        }.show()
    }
}
