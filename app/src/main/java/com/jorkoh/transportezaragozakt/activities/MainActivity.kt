package com.jorkoh.transportezaragozakt.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import com.jaredrummler.cyanea.prefs.CyaneaSettingsFragment
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.TagInfo
import com.jorkoh.transportezaragozakt.fragments.StopDetailsFragment
import com.jorkoh.transportezaragozakt.navigation.Destinations
import com.jorkoh.transportezaragozakt.navigation.goBackToPreviousDestination
import com.jorkoh.transportezaragozakt.navigation.needsCustomBackHandling
import com.jorkoh.transportezaragozakt.navigation.openDestination
import com.jorkoh.transportezaragozakt.view_models.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.destination_row.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : CyaneaAppCompatActivity() {
    private val mainActivityVM: MainActivityViewModel by viewModel()

    private val myBackStack
        get() = mainActivityVM.getCustomBackStack()

    private val onBackPressedCallback = OnBackPressedCallback {
        if (needsCustomBackHandling(myBackStack, supportFragmentManager)) {
            goBackToPreviousDestination(
                myBackStack,
                supportFragmentManager,
                R.id.fragment_container,
                bottom_navigation.menu
            )
            return@OnBackPressedCallback true
        } else {
            return@OnBackPressedCallback false
        }
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val destination = when (item.itemId) {
            R.id.navigation_favorites -> Destinations.Favorites
            R.id.navigation_map -> Destinations.Map
            R.id.navigation_search -> Destinations.Search
            R.id.navigation_more -> Destinations.More
            else -> return@OnNavigationItemSelectedListener false
        }
        openDestination(
            destination,
            myBackStack,
            supportFragmentManager,
            R.id.fragment_container
        )
        return@OnNavigationItemSelectedListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        addOnBackPressedCallback(onBackPressedCallback)
        bottom_navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        openDestination(
            myBackStack.lastOrNull() ?: Destinations.getMainDestination(),
            myBackStack,
            supportFragmentManager,
            R.id.fragment_container
        )
    }

    //@TEST
    fun openThemeSettings(@Suppress("UNUSED_PARAMETER") v: View) {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
        val currentFragment = supportFragmentManager.findFragmentByTag(myBackStack.last().getTag())
        if (currentFragment != null) {
            transaction.detach(currentFragment)
        }
        transaction.add(R.id.fragment_container, CyaneaSettingsFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    //@TEST
    fun testNotifications(@Suppress("UNUSED_PARAMETER") v: View) {


        val testRV = RemoteViews(packageName, R.layout.custom_notification)
//        testRV.setTextViewText(R.id.destination_text, "23")
//        testRV.setTextViewText(R.id.first_time_text, "2")
//        testRV.setTextViewText(R.id.second_time_text, "5")

        val testNotification = NotificationCompat.Builder(this, "TestingStuff")
            .setCustomHeadsUpContentView(testRV)
            .setCustomContentView(testRV)
            .setCustomBigContentView(testRV)
            .setSmallIcon(R.drawable.ic_bus)
            .setContentTitle("My notification")
            .setContentText("Much longer text that cannot fit one line...")
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("TestingStuff", "TestingStuff", importance).apply {
                description = "TestingStuff"
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, testNotification.build())
        }
    }

    fun openStopDetails(info: TagInfo) {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
        val currentFragment = supportFragmentManager.findFragmentByTag(myBackStack.last().getTag())
        if (currentFragment != null) {
            transaction.detach(currentFragment)
        }
        val stopDetailsFragment = StopDetailsFragment.newInstance()
        stopDetailsFragment.arguments = Bundle().apply {
            putString(StopDetailsFragment.STOP_ID_KEY, info.id)
            putString(StopDetailsFragment.STOP_TYPE_KEY, info.type.name)
        }
        transaction.add(R.id.fragment_container, stopDetailsFragment)
            .addToBackStack(null)
            .commit()
    }
}
