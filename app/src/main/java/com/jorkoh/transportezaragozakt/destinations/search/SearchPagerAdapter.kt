package com.jorkoh.transportezaragozakt.destinations.search

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.jorkoh.transportezaragozakt.R

class SearchPagerAdapter(fragmentManager: FragmentManager, private val context: Context) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment =
        when (position) {
            0 -> NearbyStopsFragment()
            1 -> AllStopsFragment()
            else -> LinesFragment()
        }

    override fun getCount(): Int = 3

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.search_nearby_stops)
            1 -> context.getString(R.string.search_all_stops)
            else -> context.getString(R.string.search_lines)
        }
    }
}