package com.jorkoh.transportezaragozakt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import com.jaredrummler.cyanea.prefs.CyaneaSettingsFragment
import com.jorkoh.transportezaragozakt.db.TagInfo
import com.jorkoh.transportezaragozakt.destinations.setupWithNavController
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragment
//import com.jorkoh.transportezaragozakt.navigation.Destinations
//import com.jorkoh.transportezaragozakt.navigation.goBackToPreviousDestination
//import com.jorkoh.transportezaragozakt.navigation.needsCustomBackHandling
//import com.jorkoh.transportezaragozakt.navigation.openDestination
import com.jorkoh.transportezaragozakt.tasks.enqueueWorker
import com.jorkoh.transportezaragozakt.tasks.setupNotificationChannels
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : CyaneaAppCompatActivity() {
    private val mainActivityVM: MainActivityViewModel by viewModel()

    private var currentNavController: LiveData<NavController>? = null

//    private val myBackStack
//        get() = mainActivityVM.getCustomBackStack()

//    private val onBackPressedCallback = OnBackPressedCallback {
//        if (needsCustomBackHandling(myBackStack, supportFragmentManager)) {
//            goBackToPreviousDestination(
//                myBackStack,
//                supportFragmentManager,
//                R.id.fragment_container,
//                bottom_navigation.menu
//            )
//            return@OnBackPressedCallback true
//        } else {
//            return@OnBackPressedCallback false
//        }
//    }
//
//    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
//        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
//        val destination = when (item.itemId) {
//            R.id.navigation_favorites -> Destinations.Favorites
//            R.id.navigation_map -> Destinations.Map
//            R.id.navigation_search -> Destinations.Search
//            R.id.navigation_more -> Destinations.More
//            else -> return@OnNavigationItemSelectedListener false
//        }
//        openDestination(
//            destination,
//            myBackStack,
//            supportFragmentManager,
//            R.id.fragment_container
//        )
//        return@OnNavigationItemSelectedListener true
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enqueueWorker()
        setupNotificationChannels(this)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState

//        addOnBackPressedCallback(onBackPressedCallback)
//        bottom_navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

//        openDestination(
//            myBackStack.lastOrNull() ?: Destinations.getMainDestination(),
//            myBackStack,
//            supportFragmentManager,
//            R.id.fragment_container
//        )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        //TODO: Add the rest of the destination graphs
        val navGraphIds = listOf(
            R.navigation.favorites_destination,
            R.navigation.map_destination,
            R.navigation.search_destination,
            R.navigation.more_destination
        )

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottom_navigation.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
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
//    fun openThemeSettings(@Suppress("UNUSED_PARAMETER") v: View) {
//        val transaction = supportFragmentManager.beginTransaction()
//            .setCustomAnimations(
//                R.anim.fade_in,
//                R.anim.fade_out,
//                R.anim.fade_in,
//                R.anim.fade_out
//            )
//        val currentFragment = supportFragmentManager.findFragmentByTag(myBackStack.last().getTag())
//        if (currentFragment != null) {
//            transaction.detach(currentFragment)
//        }
//        transaction.add(R.id.fragment_container, CyaneaSettingsFragment.newInstance())
//            .addToBackStack(null)
//            .commit()
//    }

    //@TEST
    fun testNotifications(@Suppress("UNUSED_PARAMETER") v: View) {
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

//    fun openStopDetails(info: TagInfo) {
//        val transaction = supportFragmentManager.beginTransaction()
//            .setCustomAnimations(
//                R.anim.fade_in,
//                R.anim.fade_out,
//                R.anim.fade_in,
//                R.anim.fade_out
//            )
//        val currentFragment = supportFragmentManager.findFragmentByTag(myBackStack.last().getTag())
//        if (currentFragment != null) {
//            transaction.detach(currentFragment)
//        }
//        val stopDetailsFragment = StopDetailsFragment.newInstance()
//        stopDetailsFragment.arguments = Bundle().apply {
//            putString(StopDetailsFragment.STOP_ID_KEY, info.id)
//            putString(StopDetailsFragment.STOP_TYPE_KEY, info.type.name)
//        }
//        transaction.add(R.id.fragment_container, stopDetailsFragment)
//            .addToBackStack(null)
//            .commit()
//    }
}
