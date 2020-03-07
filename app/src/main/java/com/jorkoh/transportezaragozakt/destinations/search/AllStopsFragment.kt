package com.jorkoh.transportezaragozakt.destinations.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.destinations.utils.hideKeyboard
import kotlinx.android.synthetic.main.search_destination_all_stops.*
import kotlinx.android.synthetic.main.search_destination_all_stops.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.TimeUnit

class AllStopsFragment : Fragment() {

    private val searchVM: SearchViewModel by sharedViewModel()

    private val openStop: (StopDetailsFragmentArgs, Array<Pair<View, String>>) -> Unit = { info, extras ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            activity?.currentFocus?.hideKeyboard()
            findNavController().navigate(
                SearchFragmentDirections.actionSearchToStopDetails(
                    info.stopType,
                    info.stopId
                ),
                FragmentNavigatorExtras(*extras)
            )
        }
    }

    private val allStopsAdapter = StopAdapter(openStop, {
        parentFragment?.startPostponedEnterTransition()
    })

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
        return inflater.inflate(R.layout.search_destination_all_stops, container, false).apply {
            rootView.search_recycler_view_all_stops.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = allStopsAdapter
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            allStopsAdapter.restoreInstanceState(savedInstanceState)
        }
        if (allStopsAdapter.expectsTransition && findNavController().currentDestination?.id == R.id.search) {
            // Transitioning back from StopDetailsFragment , postpone the transition animation until the destination item is ready
            parentFragment?.postponeEnterTransition(300L, TimeUnit.MILLISECONDS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        allStopsAdapter.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid leaks
        search_recycler_view_all_stops?.adapter = null
    }
}