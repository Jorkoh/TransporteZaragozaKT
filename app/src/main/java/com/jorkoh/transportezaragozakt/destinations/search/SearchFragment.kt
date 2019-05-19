package com.jorkoh.transportezaragozakt.destinations.search

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.repositories.util.observeOnce
import kotlinx.android.synthetic.main.main_container.*
import kotlinx.android.synthetic.main.search_destination.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchFragment : Fragment() {

    private val searchVM: SearchViewModel by sharedViewModel()

    private val searchTabPositionObserver = Observer<Int> { position ->
        search_tab_layout.getTabAt(position)?.select()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        searchVM.init()
        searchVM.getSearchTabPosition().observeOnce(viewLifecycleOwner, searchTabPositionObserver)
        setupToolbar()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Enables onCreateOptionsMenu() callback on activity recreation
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.search_destination, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search_viewpager.adapter = SearchPagerAdapter(childFragmentManager)
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
        Log.d("TESTING STUFF", "SETUP TOOLBAR")
        requireActivity().findViewById<Toolbar>(R.id.main_toolbar)?.let { toolbar ->
            (toolbar.menu.findItem(R.id.item_search)?.actionView as SearchView?)?.setOnQueryTextListener(null)
            toolbar.menu.clear()
            toolbar.inflateMenu(R.menu.search_destination_menu)
            (toolbar.menu.findItem(R.id.item_search).actionView as SearchView).apply {
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (!this@apply.isIconified && isVisible) {
                            searchVM.query.value = newText
                        }
                        return false
                    }
                })
                imeOptions = EditorInfo.IME_ACTION_DONE
                if (!searchVM.query.value.isNullOrEmpty()) {
                    // This entire mess is ridiculous, this still doesn't work for orientation changes and the
                    // workarounds are worse than the problem. It already is hacky enough as it is
                    // https://github.com/cbeyls/fosdem-companion-android/issues/16
                    toolbar.menu.findItem(R.id.item_search).expandActionView()
                    setQuery(searchVM.query.value, false)
                    clearFocus()
                }
            }
        }
    }
}
