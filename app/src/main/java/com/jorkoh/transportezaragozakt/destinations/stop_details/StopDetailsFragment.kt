package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.Resource
import com.jorkoh.transportezaragozakt.repositories.Status
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

    private val stopDestinationsAdapter: StopDestinationsAdapter =
        StopDestinationsAdapter()

    private lateinit var stopType: StopType

    private val stopDestinationsObserver = Observer<Resource<List<StopDestination>>> { stopDestinations ->
        updateStopDestinationsUI(stopDestinations, checkNotNull(view))
    }

    private val stopFavoriteStatusObserver = Observer<Boolean> { isFavorited ->
        updateIsFavoritedUI(isFavorited, checkNotNull(view))
    }

    private val stopTitleObserver = Observer<String> { stopTitle ->
        updateStopTitleUI(stopTitle)
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

        updateStopDestinationsUI(stopDetailsVM.getStopDestinations().value, rootView)
        stopDetailsVM.getStopDestinations().observe(this, stopDestinationsObserver)

        updateIsFavoritedUI(stopDetailsVM.stopIsFavorited.value, rootView)
        stopDetailsVM.stopIsFavorited.observe(this, stopFavoriteStatusObserver)

        updateStopTitleUI(stopDetailsVM.stopTitle.value)
        stopDetailsVM.stopTitle.observe(this, stopTitleObserver)

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

    private fun updateIsFavoritedUI(isFavorited: Boolean?, rootView: View) {
        rootView.favorite_fab.setImageDrawable(
            if (isFavorited == true) {
                resources.getDrawable(R.drawable.ic_favorite_black_24dp, null)
            } else {
                resources.getDrawable(R.drawable.ic_favorite_border_black_24dp, null)
            }
        )
    }

    private fun updateStopDestinationsUI(stopDestinations: Resource<List<StopDestination>>?, rootView: View) {
        if (stopDestinations == null) {
            return
        }

        val newVisibility = when (stopDestinations.status) {
            Status.SUCCESS -> {
                stopDestinations.data?.let { stopDestinationsAdapter.setDestinations(it, stopType) }
                rootView.swiperefresh?.isRefreshing = false
                if (stopDestinations.data.isNullOrEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            Status.ERROR -> {
                rootView.swiperefresh?.isRefreshing = false
                View.VISIBLE
            }
            Status.LOADING -> {
                rootView.swiperefresh?.isRefreshing = true
                View.GONE
            }
        }

        rootView.no_data_suggestions_text?.visibility = newVisibility
        rootView.no_data_text?.visibility = newVisibility
    }

    private fun updateStopTitleUI(stopTitle : String?){
        (requireActivity() as MainActivity).setActionBarTitle(stopTitle ?: "")
    }
}
