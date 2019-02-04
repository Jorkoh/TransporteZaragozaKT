package com.jorkoh.transportezaragozakt

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import com.jaredrummler.cyanea.prefs.CyaneaSettingsFragment
import com.jorkoh.transportezaragozakt.ViewModels.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * TODO: Learn more about Koin, Dagger and DI in general
 * val args = Bundle()
 * args.putString(FavoritesFragment.STOP_ID_KEY, "tuzsa-3063")
 * favoritesFragment.arguments = args
 */


class MainActivity : CyaneaAppCompatActivity() {
    private val mainActivityVM: MainActivityViewModel by viewModel()

    private val customBackStack
        get() = mainActivityVM.getCustomBackStack()

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_favorites -> {
                Log.d("TestingStuff", "Opened Favorites")
                openDestination(Destinations.Favorites)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_map -> {
                Log.d("TestingStuff", "Opened Map")
                openDestination(Destinations.Map)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                Log.d("TestingStuff", "Opened Search")
                openDestination(Destinations.Search)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_more -> {
                Log.d("TestingStuff", "Opened More")
                openDestination(Destinations.More)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        openDestination(customBackStack.lastOrNull() ?: Destinations.getMainDestination())
    }

    private fun openDestination(destination: Destinations) {
        val currentFragment =
            supportFragmentManager.findFragmentByTag(customBackStack.lastOrNull()?.getTag())
        var fragmentToOpen = supportFragmentManager.findFragmentByTag(destination.getTag())

        val transaction = supportFragmentManager.beginTransaction()

        // Ignore reselection of shown fragment
        if (currentFragment == fragmentToOpen && fragmentToOpen != null) {
            return
        }

        // Hide the current fragment
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        // Show the fragment to Open
        if (fragmentToOpen == null) {
            // Get an instance if it's the first time the destination is selected
            fragmentToOpen = destination.getFragment()
            transaction.add(R.id.fragment_container, fragmentToOpen, destination.getTag())
        } else {
            transaction.show(fragmentToOpen)
        }

        // Remove previous instance of this destination if exists, avoid dropping first destination like YT app
        val position = customBackStack.drop(1).indexOf(destination)
        if (position != -1) {
            customBackStack.removeAt(position + 1)
        }
        // Add the destination to the custom BackStack
        customBackStack.add(destination)

        transaction.setPrimaryNavigationFragment(fragmentToOpen)
        transaction.setReorderingAllowed(true)
        transaction.commit()
    }

    private fun goBackToPreviousDestination() {
        val destination = customBackStack[customBackStack.size - 2]
        val currentFragment =
            supportFragmentManager.findFragmentByTag(customBackStack.last().getTag())
        val fragmentToOpen = supportFragmentManager.findFragmentByTag(destination.getTag())

        val transaction = supportFragmentManager.beginTransaction()

        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        if (fragmentToOpen != null) {
            transaction.show(fragmentToOpen)
        } else {
            transaction.add(R.id.fragment_container, destination.getFragment(), destination.getTag())
        }

        transaction.setPrimaryNavigationFragment(fragmentToOpen)
        transaction.setReorderingAllowed(true)
        transaction.commit()

        customBackStack.removeAt(customBackStack.size - 1)
        bottom_navigation.menu.findItem(destination.getMenuItemID()).isChecked = true
    }

    override fun onBackPressed() {
        if (customBackStack.count() > 1 && !isDoubleHome()) {
            goBackToPreviousDestination()
        } else {
            super.onBackPressed()
        }
    }

    private fun isDoubleHome(): Boolean {
        return customBackStack.size == 2
                && customBackStack[customBackStack.size - 2] == customBackStack.last()
                && customBackStack.last() == Destinations.getMainDestination()
    }

    fun openThemeSettings(@Suppress("UNUSED_PARAMETER") v: View) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CyaneaSettingsFragment.newInstance())
            .commit()
    }
}
