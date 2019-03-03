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
import com.jorkoh.transportezaragozakt.adapters.StopDetailsAdapter
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

    private val stopDetailsAdapter: StopDetailsAdapter = StopDetailsAdapter()

    private val stopDestinationsObserver = Observer<Resource<List<StopDestination>>> { value ->
        Log.d("TestingStuff", "OBSERVED DESTINATIONS. Status: ${value.status}")
        when (value.status) {
            Status.SUCCESS -> {
                value.data?.let { stopDetailsAdapter.setDestinations(it)}
                if(value.data?.isEmpty() != false){
                    no_data_text.visibility = View.VISIBLE
                }else{
                    no_data_text.visibility = View.GONE
                }
                swiperefresh.isRefreshing = false
            }
            Status.ERROR -> {
                Toast.makeText(context, "Error loading destinations!",Toast.LENGTH_LONG).show()
                no_data_text.visibility = View.VISIBLE
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
                    resources.getDrawable(R.drawable.ic_map_black_24dp, null)
                }
            )
        }
    }

    private val onFavoritesClickListener = View.OnClickListener {
        stopDetailsVM.toggleStopFavorite()
    }

    private val onSwipeRefresListener = SwipeRefreshLayout.OnRefreshListener {
        stopDetailsVM.refreshStopDestinations()
        Log.d("TestingStuff", "HIT REFRESH")
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
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        stopDetailsVM.getStopDestinations().observe(this, stopDestinationsObserver)
        stopDetailsVM.stopIsFavorited.observe(this, stopFavoriteStatusObserver)

        rootView.favorite_fab.setOnClickListener(onFavoritesClickListener)
        rootView.swiperefresh.setOnRefreshListener(onSwipeRefresListener)

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
