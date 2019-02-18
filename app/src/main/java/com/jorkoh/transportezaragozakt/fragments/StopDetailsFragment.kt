package com.jorkoh.transportezaragozakt.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.adapters.StopDetailsAdapter
import com.jorkoh.transportezaragozakt.models.IStop
import com.jorkoh.transportezaragozakt.models.StopType
import com.jorkoh.transportezaragozakt.view_models.StopDetailsViewModel
import kotlinx.android.synthetic.main.fragment_stop_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable

class StopDetailsFragment : Fragment() {

    companion object : Serializable {
        const val STOP_ID_KEY = "STOP_ID_KEY"
        const val STOP_TYPE_KEY = "STOP_TYPE_KEY"

        @JvmStatic
        fun newInstance(): StopDetailsFragment =
            StopDetailsFragment()
    }

    private val stopDetailsVM: StopDetailsViewModel by viewModel()

    private val stopDetailsAdapter : StopDetailsAdapter =
        StopDetailsAdapter()

    private val stopObserver = Observer<IStop> { value ->
        value?.let {
            stopDetailsAdapter.setDestinations(value)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_stop_details, container, false)

        rootView.recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = stopDetailsAdapter
        }

        stopDetailsVM.getStop().observe(this, stopObserver)

        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stopDetailsVM.init(
            checkNotNull(arguments?.getString(STOP_ID_KEY)),
            StopType.valueOf(checkNotNull(arguments?.getString(STOP_TYPE_KEY)))
        )
    }

}
