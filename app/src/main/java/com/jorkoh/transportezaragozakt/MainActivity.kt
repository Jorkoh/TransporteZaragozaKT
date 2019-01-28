package com.jorkoh.transportezaragozakt

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jorkoh.transportezaragozakt.Fragments.FavoritesFragment
import com.jorkoh.transportezaragozakt.Fragments.MapFragment
import com.jorkoh.transportezaragozakt.Fragments.MoreFragment
import com.jorkoh.transportezaragozakt.Fragments.SearchFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * TODO: Fragments probably shouldn't be recreated between destinations to keep things snappy like YouTube app
 * TODO: Learn more about Koin, Dagger and DI in general
 */


class MainActivity : AppCompatActivity() {

    // Update selected item on BottomNavigationView
    private val onBackStackChangedListener = FragmentManager.OnBackStackChangedListener {
        // TODO: This must be easier to do
        val activeFragment = supportFragmentManager.primaryNavigationFragment
        if (activeFragment != null) {
            when (activeFragment::class) {
                FavoritesFragment::class -> bottom_navigation.menu.findItem(R.id.navigation_favorites)
                MapFragment::class -> bottom_navigation.menu.findItem(R.id.navigation_map)
                MoreFragment::class -> bottom_navigation.menu.findItem(R.id.navigation_more)
                SearchFragment::class -> bottom_navigation.menu.findItem(R.id.navigation_search)
                else -> return@OnBackStackChangedListener
            }.isChecked = true
        }
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_favorites -> {
                Log.d("TestingStuff", "Opened Favorites")
                showFragment(::FavoritesFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_map -> {
                Log.d("TestingStuff", "Opened Map")
                showFragment(::MapFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                Log.d("TestingStuff", "Opened Search")
                showFragment(::SearchFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_more -> {
                Log.d("TestingStuff", "Opened More")
                showFragment(::MoreFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener)
        bottom_navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

//        val favoritesFragment = FavoritesFragment.newInstance()
        // @REMOVE: Testing stuff
        // val args = Bundle()
        // args.putString(FavoritesFragment.STOP_ID_KEY, "tuzsa-3063")
        // favoritesFragment.arguments = args
        // END REMOVE
        showFragment(::FavoritesFragment)
    }

    private fun <T> showFragment(f: () -> T, firstFragment: Boolean = false) {
        val tag = f.hashCode().toString()
        val transaction = supportFragmentManager.beginTransaction()
        val currentFragment = supportFragmentManager.primaryNavigationFragment
        var fragmentToOpen = supportFragmentManager.findFragmentByTag(tag)
        if (currentFragment == fragmentToOpen) {
            return
        }

        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        if (fragmentToOpen == null) {
            fragmentToOpen = f() as Fragment
            transaction.add(R.id.fragment_container, fragmentToOpen, tag)
        } else {
            transaction.show(fragmentToOpen)
        }

        manageBackStack(transaction, tag, firstFragment)

        transaction.setPrimaryNavigationFragment(fragmentToOpen)
        transaction.setReorderingAllowed(true)
        transaction.commit()
    }

    private fun manageBackStack(transaction: FragmentTransaction, tag: String, firstFragment: Boolean) {
        if (!firstFragment) {
            transaction.addToBackStack(tag)
        }
    }
}
