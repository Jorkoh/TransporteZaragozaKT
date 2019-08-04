package com.jorkoh.transportezaragozakt.destinations.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.zxing.integration.android.IntentIntegrator
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.FragmentWithToolbar
import com.jorkoh.transportezaragozakt.repositories.util.observeOnce
import kotlinx.android.synthetic.main.search_destination.*
import kotlinx.android.synthetic.main.search_destination.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SearchFragment : FragmentWithToolbar() {

    private val searchVM: SearchViewModel by sharedViewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        searchVM.searchTabPosition.observeOnce(viewLifecycleOwner, Observer { position ->
            search_tab_layout.getTabAt(position)?.select()
        })

        search_viewpager.adapter = SearchPagerAdapter(childFragmentManager, requireContext())
        search_tab_layout.setupWithViewPager(search_viewpager)
        search_tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                // Save the selected search tab for future launches
                searchVM.setSearchTabPosition(tab.position)
            }
        })

        handleDeepLinks()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.search_destination, container, false).apply {
            setupToolbar(this)
        }
    }

    private fun setupToolbar(rootView: View) {
        rootView.fragment_toolbar.apply {
            menu.clear()
            inflateMenu(R.menu.search_destination_menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.item_scan -> {
                        // Opens the QR scanner activity
                        IntentIntegrator
                            .forSupportFragment(this@SearchFragment)
                            .setOrientationLocked(false)
                            .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                            .setPrompt(getString(R.string.qr_scan_prompt))
                            .initiateScan()
                        true
                    }
                    else -> super.onOptionsItemSelected(item)
                }
            }
            // Search action stuff
            (menu.findItem(R.id.item_search).actionView as SearchView).apply {
                queryHint = getString(R.string.search_view_hint)
                findViewById<TextView>(androidx.appcompat.R.id.search_src_text).textSize = 16f
                maxWidth = Int.MAX_VALUE
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

                if (!searchVM.query.value.isNullOrEmpty()) {
                    rootView.fragment_toolbar.menu.findItem(R.id.item_search).expandActionView()
                    setQuery(searchVM.query.value, false)
                    clearFocus()
                }
            }
        }
    }

    override fun onDestroyView() {
        // If the listener isn't removed the current listener gets a call with an empty query when the action item is destroyed
        (fragment_toolbar.menu.findItem(R.id.item_search)?.actionView as SearchView?)?.setOnQueryTextListener(null)
        super.onDestroyView()
    }

    // Handles opening stop information from successful QR scanning
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            if (result.contents.startsWith("http://www.urbanosdezaragoza.es/frm_esquemaparadatime.php?poste=")) {
                val stopId = "tuzsa-" + result.contents.split("=")[1]
                findNavController().navigate(
                    SearchFragmentDirections.actionSearchToStopDetails(
                        StopType.BUS.name,
                        stopId
                    )
                )
            } else {
                (requireActivity() as MainActivity).makeSnackbar(getString(R.string.qr_not_recognized))
            }
        }
    }

    // Handles opening stops information directly from notifications or shortcuts
    private fun handleDeepLinks() {
        // Jetpack's Navigation doesn't quite work with the multi-graph setup
        // required for bottom navigation with multi-stack so it's handled manually here
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
}
