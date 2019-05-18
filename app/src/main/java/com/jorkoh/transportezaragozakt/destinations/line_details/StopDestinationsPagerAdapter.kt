package com.jorkoh.transportezaragozakt.destinations.line_details

import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.jorkoh.transportezaragozakt.db.Line
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView


class StopDestinationsPagerAdapter(val fragmentManager: FragmentManager, private val line: Line) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment =
        when (position) {
            0 -> StopDestinationsFragment.newInstance(line.stopIdsFirstDestination)
            else -> StopDestinationsFragment.newInstance(line.stopIdsSecondDestination)
        }

    override fun getCount(): Int = if (line.stopIdsSecondDestination.isNotEmpty()) 2 else 1

    override fun getPageTitle(position: Int): CharSequence? =
        when (position) {
            0 -> line.destinations.getOrNull(0) ?: ""
            else -> line.destinations.getOrNull(1) ?: ""
        }

    //Hack around multiple nested scrolling inside bottom sheet https://stackoverflow.com/a/54098536
    override fun setPrimaryItem(container: ViewGroup, position: Int, item: Any) {
        super.setPrimaryItem(container, position, item)

        val primaryFragment = item as Fragment

        primaryFragment.view?.let {view ->
            val nestedView = view.findViewWithTag<View>("nested")
            if (nestedView != null && nestedView is RecyclerView) {
                nestedView.setNestedScrollingEnabled(true)
            }
        }

        for (fragment in fragmentManager.fragments) {
            if (fragment != primaryFragment) {
                fragment.view?.let {view ->
                    val nestedView = view.findViewWithTag<View>("nested")
                    if (nestedView != null && nestedView is RecyclerView) {
                        nestedView.setNestedScrollingEnabled(false)
                    }
                }
            }
        }
        container.requestLayout()
    }
}