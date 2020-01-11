package com.jorkoh.transportezaragozakt.destinations.line_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.line_stop_destinations.*
import kotlinx.android.synthetic.main.line_stop_destinations.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class StopsByDestinationFragment : Fragment() {

    companion object {
        const val STOP_IDS_KEY = "STOP_IDS_KEY"
        const val IS_FIRST_DESTINATION = "IS_FIRST_DESTINATION"

        fun newInstance(stopIds: List<String>, isFirstDestination: Boolean = false): StopsByDestinationFragment {
            val instance = StopsByDestinationFragment()
            instance.arguments = Bundle().apply {
                putStringArrayList(STOP_IDS_KEY, ArrayList(stopIds))
                putBoolean(IS_FIRST_DESTINATION, isFirstDestination)
            }
            return instance
        }
    }

    private val lineDetailsVM: LineDetailsViewModel by sharedViewModel(from = { parentFragment ?: error("Couldn't find parent Fragment") })

    private lateinit var stopIds: List<String>

    private val selectStop: (String) -> Unit = { stopId ->
        lifecycleScope.launchWhenStarted {
            lineDetailsVM.selectedItemId.send(stopId)
        }
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
            itemAnimator = null
        }

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        stopIds = arguments?.getStringArrayList(STOP_IDS_KEY)?.toList().orEmpty()
        val isFirstDestination = arguments?.getBoolean(IS_FIRST_DESTINATION) ?: false

        lineDetailsVM.stops.observe(viewLifecycleOwner, Observer { allStops ->
            if (allStops != null) {
                // Filter those with this destination
                lifecycleScope.launch {
                    val sortedStops = withContext(Dispatchers.Default) {
                        val stops = allStops.filter { it.stopId in stopIds }
                        val orderById = stopIds.withIndex().associate { it.value to it.index }
                        stops.sortedBy { orderById[it.stopId] }
                    }
                    stopsAdapter.setNewStops(sortedStops)

                    if (isFirstDestination) {
                        parentFragment?.startPostponedEnterTransition()
                    }
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