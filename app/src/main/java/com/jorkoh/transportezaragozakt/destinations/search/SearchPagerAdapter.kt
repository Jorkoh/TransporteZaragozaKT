package com.jorkoh.transportezaragozakt.destinations.search

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> NearbyStopsFragment()
            1 -> AllStopsFragment()
            else -> LinesFragment()
        }
    }

    override fun getCount(): Int = 3

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "NEARBY STOPS"
            1 -> "ALL STOPS"
            else -> "LINES"
        }
    }
}