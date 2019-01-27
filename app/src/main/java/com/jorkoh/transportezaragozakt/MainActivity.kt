package com.jorkoh.transportezaragozakt

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_favorites -> {
                Log.d("TestingStuff", "Opened Favorites")
                val favoritesFragment = FavoritesFragment.newInstance()
                // @REMOVE: Testing stuff
                val args = Bundle()
                args.putString(FavoritesFragment.STOP_ID_KEY, "tuzsa-3063")
                favoritesFragment.arguments = args
                // END REMOVE
                openFragment(favoritesFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_map -> {
                Log.d("TestingStuff", "Opened Map")
                val mapFragment = MapFragment.newInstance()
                openFragment(mapFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                Log.d("TestingStuff", "Opened Search")
                val searchFragment = SearchFragment.newInstance()
                openFragment(searchFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_more -> {
                Log.d("TestingStuff", "Opened More")
                val moreFragment = MoreFragment.newInstance()
                openFragment(moreFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        val favoritesFragment = FavoritesFragment.newInstance()
        // @REMOVE: Testing stuff
        val args = Bundle()
        args.putString(FavoritesFragment.STOP_ID_KEY, "tuzsa-3063")
        favoritesFragment.arguments = args
        // END REMOVE
        // TODO: This could be done differently?
        openFragment(favoritesFragment, false)
    }

    private fun openFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }
}
