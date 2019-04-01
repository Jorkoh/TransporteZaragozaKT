package com.jorkoh.transportezaragozakt.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.adapters.StopDestinationsAdapter
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.Resource
import com.jorkoh.transportezaragozakt.repositories.Status
import com.jorkoh.transportezaragozakt.view_models.StopDetailsViewModel
import kotlinx.android.synthetic.main.fragment_stop_details.*
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

    private val stopDestinationsAdapter: StopDestinationsAdapter = StopDestinationsAdapter()

    private lateinit var stopType : StopType

    private val stopDestinationsObserver = Observer<Resource<List<StopDestination>>> { value ->
        when (value.status) {
            Status.SUCCESS -> {
                value.data?.let { stopDestinationsAdapter.setDestinations(it, stopType)}
                if(value.data?.isEmpty() != false){
                    no_data_text.visibility = View.VISIBLE
                    no_data_suggestions_text.visibility = View.VISIBLE
                }else{
                    no_data_text.visibility = View.GONE
                    no_data_suggestions_text.visibility = View.GONE
                }
                swiperefresh.isRefreshing = false
            }
            Status.ERROR -> {
                Toast.makeText(context, "Error loading destinations!",Toast.LENGTH_LONG).show()
                no_data_text.visibility = View.VISIBLE
                no_data_suggestions_text.visibility = View.VISIBLE
                swiperefresh.isRefreshing = false
            }
            Status.LOADING -> swiperefresh.isRefreshing = true
        }

    }

    private val stopFavoriteStatusObserver = Observer<Boolean> { isFavorited ->
        isFavorited?.let {
            favorite_fab.setImageDrawable(
                if (isFavorited) {
                    resources.getDrawable(R.drawable.ic_favorite_black_24dp, null)
                } else {
                    resources.getDrawable(R.drawable.ic_favorite_border_black_24dp, null)
                }
            )
        }
    }

    private val onFavoritesClickListener = View.OnClickListener {
        stopDetailsVM.toggleStopFavorite()
    }

    private val onSwipeRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        stopDetailsVM.refreshStopDestinations()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_stop_details, container, false)

        rootView.recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = stopDestinationsAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        stopDetailsVM.getStopDestinations().observe(this, stopDestinationsObserver)
        stopDetailsVM.stopIsFavorited.observe(this, stopFavoriteStatusObserver)

        rootView.favorite_fab.setOnClickListener(onFavoritesClickListener)
        rootView.swiperefresh.setOnRefreshListener(onSwipeRefreshListener)

        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stopType = StopType.valueOf(checkNotNull(arguments?.getString(STOP_TYPE_KEY)))
        stopDetailsVM.init(
            checkNotNull(arguments?.getString(STOP_ID_KEY)),
            stopType
        )
    }
}
