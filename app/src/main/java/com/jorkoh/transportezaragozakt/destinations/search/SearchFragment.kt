package com.jorkoh.transportezaragozakt.destinations.search

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.main_container.*
import kotlinx.android.synthetic.main.search_destination.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private val searchVM: SearchViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.search_destination, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TESTING STUFF", "SETTING SEARCH BAR LISTENER")

        (requireActivity().toolbar.menu.findItem(R.id.item_search).actionView as SearchView).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    Log.d("TESTING STUFF", "Text submit: $query")
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d("TESTING STUFF", "Text submit: $newText")
                    return false
                }

            })
            imeOptions = EditorInfo.IME_ACTION_DONE
        }

        search_viewpager.adapter = TestPagerAdapter(childFragmentManager)
        search_tab_layout.setupWithViewPager(search_viewpager)
    }

    class TestPagerAdapter(fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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
}
