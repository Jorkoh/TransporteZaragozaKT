package com.jorkoh.transportezaragozakt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_favorites -> {
                Log.d("TestingStuff", "Opened Favorites")
                val favoritesFragment = FavoritesFragment.newInstance()
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
        openFragment(favoritesFragment)
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
