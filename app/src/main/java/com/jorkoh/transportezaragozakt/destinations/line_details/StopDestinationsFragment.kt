package com.jorkoh.transportezaragozakt.destinations.line_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.line_stop_destinations.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class StopDestinationsFragment(private val stopIds: List<String>) : Fragment() {

    private val lineDetailsVM: LineDetailsViewModel by sharedViewModel()

    private val selectStop: (String) -> Unit = { stopId ->
        lineDetailsVM.selectedStopId.postValue(stopId)
    }

    private val stopsAdapter = StopDestinationsAdapter(selectStop)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.line_stop_destinations, container, false)

        rootView.line_recycler_view_stop_destinations.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = stopsAdapter
        }
        val stops = lineDetailsVM.stops.value?.filter { it.stopId in stopIds }
        if (!stops.isNullOrEmpty()) {
            stopsAdapter.setNewStops(stops)
        }

        return rootView
    }

}