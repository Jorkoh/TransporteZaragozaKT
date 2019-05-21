package com.jorkoh.transportezaragozakt.destinations.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.zxing.integration.android.IntentIntegrator
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        searchVM.init()
        searchVM.getSearchTabPosition().observeOnce(viewLifecycleOwner, searchTabPositionObserver)
        setupToolbar()
        search_viewpager.adapter = SearchPagerAdapter(childFragmentManager, requireContext())
        search_tab_layout.setupWithViewPager(search_viewpager)
        search_tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                searchVM.setSearchTabPosition(tab.position)
            }
        })
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
        requireActivity().intent = requireActivity().intent.apply {
            dataString?.let { uri ->
                if (uri.startsWith("launchTZ://viewStop")) {
                    val splitUri = uri.split("/")
                    findNavController().navigate(
                        SearchFragmentDirections.actionSearchToStopDetails(
                            splitUri[3],
                            splitUri[4]
                        )
                    )
                    data = Uri.EMPTY
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        setupToolbar()
        super.onCreateOptionsMenu(menu, inflater)
    }

    //Has to be called from both onCreateView() and onCreateOptionsMenu() to avoid problems with bottom navigation
    private fun setupToolbar() {
        requireActivity().findViewById<Toolbar>(R.id.main_toolbar)?.let { toolbar ->
            (toolbar.menu.findItem(R.id.item_search)?.actionView as SearchView?)?.setOnQueryTextListener(null)
            toolbar.menu.clear()
            toolbar.inflateMenu(R.menu.search_destination_menu)
            (toolbar.menu.findItem(R.id.item_search).actionView as SearchView).apply {
                this.queryHint = getString(R.string.search_view_hint)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_scan -> {
//                findNavController().navigate(SearchFragmentDirections.actionSearchToScannerFragment())
                IntentIntegrator
                    .forSupportFragment(this)
                    .setOrientationLocked(false)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    .setPrompt(getString(R.string.qr_scan_prompt))
                    .initiateScan()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            val toastText = if (result.contents == null) {
                "Cancelled from fragment"
            } else {
                "Scanned from fragment: " + result.contents
            }
            Toast.makeText(activity, toastText, Toast.LENGTH_LONG).show()
        }
        fragmentManager?.popBackStack()
    }
}
