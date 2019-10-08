package com.jorkoh.transportezaragozakt.destinations.line_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.line_stop_destinations.*
import kotlinx.android.synthetic.main.line_stop_destinations.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class StopsByDestinationFragment : Fragment() {

    companion object {
        const val STOP_IDS_KEY = "STOP_IDS_KEY"

        fun newInstance(stopIds: List<String>): StopsByDestinationFragment {
            val instance = StopsByDestinationFragment()
            instance.arguments = Bundle().apply {
                putStringArrayList(STOP_IDS_KEY, ArrayList(stopIds))
            }
            return instance
        }
    }

    private val lineDetailsVM: LineDetailsViewModel by sharedViewModel(from = {
        parentFragment ?: error("Couldn't find parent Fragment")
    })

    private lateinit var stopIds: List<String>

    private val selectStop: (String) -> Unit = { stopId ->
        lineDetailsVM.selectedStopId.postValue(stopId)
    }

    private val stopsAdapter = StopsByDestinationAdapter(selectStop)

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

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        stopIds = arguments?.getStringArrayList(STOP_IDS_KEY)?.toList().orEmpty()
        lineDetailsVM.stops.observe(viewLifecycleOwner, Observer { allStops ->
            allStops?.let {
                // Filter those with this destination
                val stops = lineDetailsVM.stops.value?.filter { it.stopId in stopIds }
                val orderById = stopIds.withIndex().associate { it.value to it.index }
                val sortedStops = stops?.sortedBy { orderById[it.stopId] }
                if (!sortedStops.isNullOrEmpty()) {
                    stopsAdapter.setNewStops(sortedStops)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid leaks
        line_recycler_view_stop_destinations?.adapter = null
    }
}