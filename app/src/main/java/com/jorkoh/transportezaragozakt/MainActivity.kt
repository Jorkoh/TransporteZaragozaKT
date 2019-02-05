package com.jorkoh.transportezaragozakt

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import com.jaredrummler.cyanea.prefs.CyaneaSettingsFragment
import com.jorkoh.transportezaragozakt.ViewModels.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * val args = Bundle()
 * args.putString(FavoritesFragment.STOP_ID_KEY, "tuzsa-3063")
 * favoritesFragment.arguments = args
 */


class MainActivity : CyaneaAppCompatActivity() {
    private val mainActivityVM: MainActivityViewModel by viewModel()

    private val myBackStack
        get() = mainActivityVM.getCustomBackStack()

    private val onBackPressedCallback = OnBackPressedCallback {
        Log.d("TestingStuff", "On back pressed callback")
        if (supportFragmentManager.backStackEntryCount == 0 && myBackStack.count() > 1 && !isDoubleHome()) {
            goBackToPreviousDestination()
            return@OnBackPressedCallback true
        } else {
            return@OnBackPressedCallback false
        }
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        //@TODO: Support multiple backstacks like YT app
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
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

        addOnBackPressedCallback(onBackPressedCallback)
        bottom_navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        openDestination(myBackStack.lastOrNull() ?: Destinations.getMainDestination())
    }

    private fun openDestination(destination: Destinations) {
        val currentFragment = supportFragmentManager.findFragmentByTag(myBackStack.lastOrNull()?.getTag())
        var fragmentToOpen = supportFragmentManager.findFragmentByTag(destination.getTag())
        val transaction = supportFragmentManager.beginTransaction()
        // Ignore reselection of shown fragment
        if (currentFragment == fragmentToOpen && fragmentToOpen != null) {
            return
        }
        // Hide the current fragment if it exists
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        // Show the fragment to Open
        if (fragmentToOpen == null) {
            // Get an instance if it's the first time the destination is selected
            transaction.add(R.id.fragment_container, destination.getFragment(), destination.getTag())
        } else {
            transaction.show(fragmentToOpen)
        }
        // Remove previous instance of this destination if exists, avoid dropping first destination like YT app
        val position = myBackStack.drop(1).indexOf(destination)
        if (position != -1) {
            myBackStack.removeAt(position + 1)
        }
        // Add the destination to the custom BackStack
        myBackStack.add(destination)
        // Commit the transaction
        transaction.setPrimaryNavigationFragment(fragmentToOpen)
        transaction.setReorderingAllowed(true)
        transaction.commit()
    }

    private fun goBackToPreviousDestination() {
        val destination = myBackStack[myBackStack.size - 2]
        val currentFragment = supportFragmentManager.findFragmentByTag(myBackStack.last().getTag())
        val fragmentToOpen = supportFragmentManager.findFragmentByTag(destination.getTag())
        val transaction = supportFragmentManager.beginTransaction()
        // Hide current if it exists
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        // Show the fragment to open
        if (fragmentToOpen != null) {
            transaction.show(fragmentToOpen)
        } else {
            // Get an instance if it somehow doesn't exists
            transaction.add(R.id.fragment_container, destination.getFragment(), destination.getTag())
        }
        // Remove the destination from the custom BackStack
        myBackStack.removeAt(myBackStack.size - 1)
        // Manually check the destination on the bottom navigation bar
        bottom_navigation.menu.findItem(destination.getMenuItemID()).isChecked = true
        // Commit the transaction
        transaction.setPrimaryNavigationFragment(fragmentToOpen)
        transaction.setReorderingAllowed(true)
        transaction.commit()
    }

    private fun isDoubleHome(): Boolean {
        return myBackStack.size == 2
                && myBackStack[myBackStack.size - 2] == myBackStack.last()
                && myBackStack.last() == Destinations.getMainDestination()
    }

    fun openThemeSettings(@Suppress("UNUSED_PARAMETER") v: View) {
        val transaction = supportFragmentManager.beginTransaction()
        val currentFragment = supportFragmentManager.findFragmentByTag(myBackStack.last().getTag())
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.add(R.id.fragment_container, CyaneaSettingsFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }
}
