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
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.SearchDestinationDirections
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopWithDistance
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import kotlinx.android.synthetic.main.search_destination_nearby_stops.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class NearbyStopsFragment : Fragment(){

    private val searchVM: SearchViewModel by sharedViewModel()

    private val openStop: (StopDetailsFragmentArgs) -> Unit = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            findNavController().navigate(
                SearchDestinationDirections.actionGlobalStopDetails(
                    info.stopType,
                    info.stopId
                )
            )
        }
    }

    private val nearbyStopsAdapter = StopWithDistanceAdapter(openStop)

    private val nearbyStopsObserver = Observer<List<StopWithDistance>> { nearbyStops ->
        updateEmptyViewVisibility(nearbyStops.isEmpty())
        nearbyStopsAdapter.setNewStops(nearbyStops)
        nearbyStopsAdapter.filter.filter(searchVM.query.value)
    }

    private val queryObserver = Observer<String?> { query ->
        nearbyStopsAdapter.filter.filter(query) { flag ->
            //If the list went from actually filtered to initial state scroll back up to the top
            if (query == "" && flag == 1) {
                (view?.search_recycler_view_nearby_stops?.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(0, 0)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        searchVM.nearbyStops.observe(viewLifecycleOwner, nearbyStopsObserver)
        searchVM.query.observe(viewLifecycleOwner, queryObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.search_destination_nearby_stops, container, false)

        rootView.search_recycler_view_nearby_stops.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = nearbyStopsAdapter
        }

        return rootView
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean) {
//        val newVisibility = if (isEmpty) {
//            View.VISIBLE
//        } else {
//            View.GONE
//        }
//        view?.no_search_result_animation_nearby_stops?.visibility = newVisibility
//        view?.no_search_result_text_nearby_stops?.visibility = newVisibility
    }
}