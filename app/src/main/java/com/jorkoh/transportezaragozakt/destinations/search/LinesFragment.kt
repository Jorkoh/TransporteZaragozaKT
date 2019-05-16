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
import com.jorkoh.transportezaragozakt.db.Line
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsFragmentArgs
import kotlinx.android.synthetic.main.search_destination_lines.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LinesFragment : Fragment(){

    private val searchVM: SearchViewModel by sharedViewModel()

    private val openLine: (LineDetailsFragmentArgs) -> Unit = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            findNavController().navigate(
                SearchFragmentDirections.actionGlobalLineDetails(
                    info.lineType,
                    info.lineId
                )
            )
        }
    }

    private val linesAdapter = LineAdapter(openLine)

    private val linesObserver = Observer<List<Line>> { lines ->
        updateEmptyViewVisibility(lines.isEmpty())
        linesAdapter.setNewLines(lines)
        linesAdapter.filter.filter(searchVM.query.value)
    }

    private val queryObserver = Observer<String?> { query ->
        linesAdapter.filter.filter(query) { flag ->
            //If the list went from actually filtered to initial state scroll back up to the top
            if (query == "" && flag == 1) {
                (view?.search_recycler_view_lines?.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(0, 0)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        searchVM.lines.observe(viewLifecycleOwner, linesObserver)
        searchVM.query.observe(viewLifecycleOwner, queryObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.search_destination_lines, container, false)

        rootView.search_recycler_view_lines.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = linesAdapter
        }

        return rootView
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean) {
//        val newVisibility = if (isEmpty) {
//            View.VISIBLE
//        } else {
//            View.GONE
//        }
//        view?.no_search_result_animation_all_stops?.visibility = newVisibility
//        view?.no_search_result_text_all_stops?.visibility = newVisibility
    }
}