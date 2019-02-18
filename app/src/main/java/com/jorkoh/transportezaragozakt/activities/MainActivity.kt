package com.jorkoh.transportezaragozakt.activities

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import com.jaredrummler.cyanea.prefs.CyaneaSettingsFragment
import com.jorkoh.transportezaragozakt.navigation.Destinations
import com.jorkoh.transportezaragozakt.fragments.StopDetailsFragment
import com.jorkoh.transportezaragozakt.models.StopType
import com.jorkoh.transportezaragozakt.navigation.goBackToPreviousDestination
import com.jorkoh.transportezaragozakt.navigation.needsCustomBackHandling
import com.jorkoh.transportezaragozakt.navigation.openDestination
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.view_models.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
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
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
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
    fun openDetailsTest(@Suppress("UNUSED_PARAMETER") v: View) {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        val currentFragment = supportFragmentManager.findFragmentByTag(myBackStack.last().getTag())
        if (currentFragment != null) {
            transaction.detach(currentFragment)
        }
        val stopDetailsFragment = StopDetailsFragment.newInstance()
        stopDetailsFragment.arguments = Bundle().apply {
//            putString(StopDetailsFragment.STOP_ID_KEY, "tuzsa-3063")
//            putString(StopDetailsFragment.STOP_TYPE_KEY, StopType.BUS.name)
            putString(StopDetailsFragment.STOP_ID_KEY, "202")
            putString(StopDetailsFragment.STOP_TYPE_KEY, StopType.TRAM.name)
        }
        transaction.add(R.id.fragment_container, stopDetailsFragment)
            .addToBackStack(null)
            .commit()
    }
}
