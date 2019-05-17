package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.repositories.util.Status
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.android.synthetic.main.stop_details_destination.*
import kotlinx.android.synthetic.main.stop_details_destination.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable
import android.graphics.drawable.Icon
import android.os.Build.VERSION_CODES.O
import android.util.Log
import android.view.*
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.input.input
import com.jorkoh.transportezaragozakt.FavoritesDestinationDirections
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsFragmentArgs
import kotlinx.android.synthetic.main.main_container.*


class StopDetailsFragment : Fragment() {
    companion object : Serializable {
        const val FAVORITE_ITEM_FAB_POSITION = 0
    }

    private val stopDetailsVM: StopDetailsViewModel by viewModel()

    private val openLine: (LineDetailsFragmentArgs) -> Unit = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            findNavController().navigate(
                FavoritesDestinationDirections.actionGlobalLineDetails(
                    info.lineType,
                    info.lineId
                )
            )
        }
    }

    private val stopDestinationsAdapter: StopDestinationsAdapter = StopDestinationsAdapter(openLine)

    private val stopDestinationsObserver = Observer<Resource<List<StopDestination>>> { stopDestinations ->
        val newVisibility = when (stopDestinations.status) {
            Status.SUCCESS -> {
                swiperefresh?.isRefreshing = false
                stopDestinationsAdapter.setDestinations(stopDestinations.data.orEmpty(), stopDetailsVM.stopType)
                if (stopDestinations.data.isNullOrEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            Status.ERROR -> {
                swiperefresh?.isRefreshing = false
                stopDestinationsAdapter.setDestinations(listOf(), stopDetailsVM.stopType)
                View.VISIBLE

            }
            Status.LOADING -> {
                swiperefresh?.isRefreshing = true
                View.GONE
            }
        }

        no_data_suggestions_text?.visibility = newVisibility
        no_data_text?.visibility = newVisibility
    }

    private val stopFavoriteStatusObserver = Observer<Boolean> { isFavorited ->
        val newIcon = if (isFavorited) {
            R.drawable.ic_favorite_black_24dp
        } else {
            R.drawable.ic_favorite_border_black_24dp
        }

        val newLabel = if (isFavorited) {
            R.string.fab_remove_favorite
        } else {
            R.string.fab_add_favorite
        }

        stop_details_fab.replaceActionItem(
            SpeedDialActionItem.Builder(R.id.stop_details_fab_favorite, newIcon)
                .setLabel(newLabel)
                .create(),
            FAVORITE_ITEM_FAB_POSITION
        )
    }

    private val onSwipeRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        stopDetailsVM.refreshStopDestinations()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.stop_details_destination, container, false)

        setupToolbar()
        setupFab(rootView)

        rootView.favorites_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = stopDestinationsAdapter
        }

        rootView.swiperefresh.setOnRefreshListener(onSwipeRefreshListener)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val args = StopDetailsFragmentArgs.fromBundle(requireArguments())
        stopDetailsVM.init(args.stopId, StopType.valueOf(args.stopType))

        stopDetailsVM.stopDestinations.observe(viewLifecycleOwner, stopDestinationsObserver)
        stopDetailsVM.refreshStopDestinations()
        stopDetailsVM.stopIsFavorited.observe(viewLifecycleOwner, stopFavoriteStatusObserver)
        stopDetailsVM.stopTitle.observe(viewLifecycleOwner, Observer { stopTitle ->
            (requireActivity() as MainActivity).setActionBarTitle(stopTitle)
        })
    }

    private fun setupToolbar() {
        requireActivity().main_toolbar.menu.clear()
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
            if (SDK_INT >= O) {
                addActionItem(
                    SpeedDialActionItem.Builder(
                        R.id.stop_details_fab_shortcut,
                        R.drawable.ic_launch_black_24dp
                    )
                        .setLabel(R.string.fab_shortcut)
                        .create()
                )
            }

            setOnActionSelectedListener { actionItem ->
                when (actionItem.id) {
                    R.id.stop_details_fab_favorite -> {
                        stopDetailsVM.toggleStopFavorite()
                        true
                    }
                    R.id.stop_details_fab_reminder -> {
                        MaterialDialog(requireContext()).show {
                            title(R.string.create_reminder)
                            timePicker(
                                show24HoursView = false,
                                daysOfWeek = listOf(true, true, true, true, true, false, false)
                            ) { _, time, daysOfWeek ->
                                stopDetailsVM.createReminder(daysOfWeek, time)
                            }
                            positiveButton(R.string.create_button)
                        }
                        true
                    }
                    R.id.stop_details_fab_shortcut -> {
                        MaterialDialog(requireContext()).show {
                            title(R.string.create_shortcut)
                            input(prefill = stopDetailsVM.stopTitle.value) { _, text ->
                                createShortcut(text.toString())
                            }
                            positiveButton(R.string.create_button)
                        }
                        true
                    }
                    else -> true
                }
            }
        }
    }

    private fun createShortcut(label: String) {
        if (SDK_INT >= O) {
            val shortcutManager = requireContext().getSystemService(ShortcutManager::class.java)
            val shortcut = ShortcutInfo.Builder(requireContext(), stopDetailsVM.stopId)
                .setShortLabel(label)
                .setIcon(
                    Icon.createWithResource(
                        requireContext(),
                        when (stopDetailsVM.stopType) {
                            StopType.BUS -> R.mipmap.ic_bus_launcher
                            StopType.TRAM -> R.mipmap.ic_tram_launcher
                        }
                    )
                )
                .setIntent(createStopDetailsDeepLink(stopDetailsVM.stopId, stopDetailsVM.stopType))
                .build()
            if (shortcutManager.isRequestPinShortcutSupported) {
                shortcutManager.requestPinShortcut(shortcut, null)
            } else {
                Snackbar.make(
                    stop_details_fab,
                    R.string.shortcut_pinning_not_supported,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}
