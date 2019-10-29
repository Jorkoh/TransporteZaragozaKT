package com.jorkoh.transportezaragozakt.destinations.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.destinations.hideKeyboard
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import kotlinx.android.synthetic.main.search_destination_all_stops.*
import kotlinx.android.synthetic.main.search_destination_all_stops.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AllStopsFragment : Fragment() {

    private val searchVM: SearchViewModel by sharedViewModel()

    private val openStop: (StopDetailsFragmentArgs) -> Unit = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            activity?.currentFocus?.hideKeyboard()
            findNavController().navigate(
                SearchFragmentDirections.actionSearchToStopDetails(
                    info.stopType,
                    info.stopId
                )
            )
        }
    }

    private val allStopsAdapter = StopAdapter(openStop)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        searchVM.allStops.observe(viewLifecycleOwner, Observer { allStops ->
            allStopsAdapter.setNewStops(allStops, searchVM.query.value)
        })
        searchVM.query.observe(viewLifecycleOwner, Observer { query ->
            allStopsAdapter.filter.filter(query) { flag ->
                // If the list went from actually filtered to initial state scroll back up to the top
                if (query == "" && flag == 1) {
                    (view?.search_recycler_view_all_stops?.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(0, 0)
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.search_destination_all_stops, container, false)

        rootView.search_recycler_view_all_stops.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = allStopsAdapter
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid leaks
        search_recycler_view_all_stops?.adapter = null
    }
}