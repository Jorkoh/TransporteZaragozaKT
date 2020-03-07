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
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.destinations.utils.hideKeyboard
import kotlinx.android.synthetic.main.search_destination_lines.*
import kotlinx.android.synthetic.main.search_destination_lines.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.TimeUnit

class LinesFragment : Fragment() {
    private val searchVM: SearchViewModel by sharedViewModel()

    private val openLine: (LineDetailsFragmentArgs, Array<Pair<View, String>>) -> Unit = { info, extras ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            activity?.currentFocus?.hideKeyboard()
            findNavController().navigate(
                SearchFragmentDirections.actionSearchToLineDetails(
                    info.lineType,
                    info.lineId,
                    info.stopId
                ),
                FragmentNavigatorExtras(*extras)
            )
        }
    }

    private val linesAdapter = LineAdapter(openLine, {
        parentFragment?.startPostponedEnterTransition()
    })

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        searchVM.mainLines.observe(viewLifecycleOwner, Observer { lines ->
            linesAdapter.setNewLines(lines, searchVM.query.value)
        })
        searchVM.query.observe(viewLifecycleOwner, Observer { query ->
            linesAdapter.filter.filter(query) { flag ->
                // If the list went from actually filtered to initial state scroll back up to the top
                if (query == "" && flag == 1) {
                    (view?.search_recycler_view_lines?.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(0, 0)
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.search_destination_lines, container, false).apply {
            search_recycler_view_lines.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = linesAdapter
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            linesAdapter.restoreInstanceState(savedInstanceState)
        }
        if (linesAdapter.expectsTransition && findNavController().currentDestination?.id == R.id.search) {
            // Transitioning back from StopDetailsFragment , postpone the transition animation until the destination item is ready
            parentFragment?.postponeEnterTransition(300L, TimeUnit.MILLISECONDS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        linesAdapter.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid leaks
        search_recycler_view_lines?.adapter = null
    }
}