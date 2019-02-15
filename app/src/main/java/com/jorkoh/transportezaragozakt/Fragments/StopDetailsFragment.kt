package com.jorkoh.transportezaragozakt.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jorkoh.transportezaragozakt.Adapters.StopDetailsAdapter
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.ViewModels.StopDetailsViewModel
import kotlinx.android.synthetic.main.fragment_stop_details.*
import kotlinx.android.synthetic.main.fragment_stop_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class StopDetailsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(): StopDetailsFragment =
            StopDetailsFragment()
    }

    private val stopDetailsVM: StopDetailsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_stop_details, container, false)
        rootView.recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = StopDetailsAdapter(mutableListOf("test1", "test2", "test3", "test4"))
        }

        return rootView
    }

}
