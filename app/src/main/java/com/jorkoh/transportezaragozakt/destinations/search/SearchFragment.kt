package com.jorkoh.transportezaragozakt.destinations.search

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.repositories.util.observeOnce
import kotlinx.android.synthetic.main.main_container.*
import kotlinx.android.synthetic.main.search_destination.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private val searchVM: SearchViewModel by sharedViewModel()

    private val searchTabPositionObserver = Observer<Int> { position ->
        search_tab_layout.getTabAt(position)?.select()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        searchVM.init()
        searchVM.getSearchTabPosition().observeOnce(viewLifecycleOwner, searchTabPositionObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Enables onCreateOptionsMenu() callback on activity recreation
        setHasOptionsMenu(true)
        setupToolbar()
        return inflater.inflate(R.layout.search_destination, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search_viewpager.adapter = TestPagerAdapter(childFragmentManager)
        search_tab_layout.setupWithViewPager(search_viewpager)
        search_tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                searchVM.setSearchTabPosition(tab.position)
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        setupToolbar()
        super.onCreateOptionsMenu(menu, inflater)
    }

    //Has to be called from both onCreateView() and onCreateOptionsMenu() to avoid problems with bottom navigation
    private fun setupToolbar() {
        requireActivity().main_toolbar.apply {
            menu.clear()
            inflateMenu(R.menu.search_destination_menu)
            (menu.findItem(R.id.item_search).actionView as SearchView).apply {
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        searchVM.query.value = newText
                        return false
                    }

                })
                imeOptions = EditorInfo.IME_ACTION_DONE
            }
        }
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
