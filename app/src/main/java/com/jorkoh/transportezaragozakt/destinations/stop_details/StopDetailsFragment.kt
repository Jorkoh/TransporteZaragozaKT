package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.Resource
import com.jorkoh.transportezaragozakt.repositories.Status
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.android.synthetic.main.stop_details.*
import kotlinx.android.synthetic.main.stop_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable

class StopDetailsFragment : Fragment() {
    companion object : Serializable {
        const val STOP_ID_KEY = "STOP_ID_KEY"
        const val STOP_TYPE_KEY = "STOP_TYPE_KEY"

        const val FAVORITE_ITEM_FAB_POSITION = 0
    }

    private val stopDetailsVM: StopDetailsViewModel by viewModel()

    private val stopDestinationsAdapter: StopDestinationsAdapter =
        StopDestinationsAdapter()

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
        val rootView = inflater.inflate(R.layout.stop_details, container, false)

        setupFab(rootView)

        rootView.favorites_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = stopDestinationsAdapter
        }

        updateStopDestinationsUI(stopDetailsVM.getStopDestinations().value, rootView)
        stopDetailsVM.getStopDestinations().observe(this, stopDestinationsObserver)

        updateIsFavoritedUI(stopDetailsVM.stopIsFavorited.value, rootView)
        stopDetailsVM.stopIsFavorited.observe(this, stopFavoriteStatusObserver)

        updateStopTitleUI(stopDetailsVM.stopTitle.value)
        stopDetailsVM.stopTitle.observe(this, stopTitleObserver)

        rootView.stop_details_fab.setOnClickListener(onFavoritesClickListener)
        rootView.swiperefresh.setOnRefreshListener(onSwipeRefreshListener)

        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stopDetailsVM.init(
            checkNotNull(arguments?.getString(STOP_ID_KEY)),
            StopType.valueOf(checkNotNull(arguments?.getString(STOP_TYPE_KEY)))
        )
    }

    private fun setupFab(rootView: View) {
        rootView.stop_details_fab.apply {
            addActionItem(
                SpeedDialActionItem.Builder(
                    R.id.stop_details_fab_favorite,
                    R.drawable.ic_favorite_border_black_24dp
                )
                    .setLabel(R.string.fab_add_favorite)
                    .create()
            )
            addActionItem(
                SpeedDialActionItem.Builder(
                    R.id.stop_details_fab_reminder,
                    R.drawable.ic_add_alarm_black_24dp
                )
                    .setLabel(R.string.fab_reminder)
                    .create()
            )

            setOnActionSelectedListener { actionItem ->
                when (actionItem.id) {
                    R.id.stop_details_fab_favorite -> {
                        stopDetailsVM.toggleStopFavorite()
                        true
                    }
                    R.id.stop_details_fab_reminder -> true
                    else -> true
                }
            }
        }
    }

    private fun updateIsFavoritedUI(isFavorited: Boolean?, rootView: View) {
        val newIcon = if (isFavorited == true) {
            R.drawable.ic_favorite_black_24dp
        } else {
            R.drawable.ic_favorite_border_black_24dp
        }

        rootView.stop_details_fab.replaceActionItem(
            SpeedDialActionItem.Builder(R.id.stop_details_fab_favorite, newIcon)
                .setLabel(R.string.fab_add_favorite)
                .create(),
            FAVORITE_ITEM_FAB_POSITION
        )
    }

    private fun updateStopDestinationsUI(stopDestinations: Resource<List<StopDestination>>?, rootView: View) {
        if (stopDestinations == null) {
            return
        }

        val newVisibility = when (stopDestinations.status) {
            Status.SUCCESS -> {
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
        stopDestinationsAdapter.setDestinations(stopDestinations.data.orEmpty(), stopDetailsVM.stopType)

        rootView.no_data_suggestions_text?.visibility = newVisibility
        rootView.no_data_text?.visibility = newVisibility
    }

    private fun updateStopTitleUI(stopTitle: String?) {
        (requireActivity() as MainActivity).setActionBarTitle(stopTitle ?: "")
    }
}
