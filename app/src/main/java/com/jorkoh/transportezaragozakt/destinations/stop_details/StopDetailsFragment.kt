package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.input.input
import com.google.android.material.snackbar.Snackbar
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.repositories.util.Status
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.pixplicity.generate.Rate
import kotlinx.android.synthetic.main.main_container.*
import kotlinx.android.synthetic.main.stop_details_destination.*
import kotlinx.android.synthetic.main.stop_details_destination.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable


class StopDetailsFragment : Fragment() {
    companion object : Serializable {
        const val FAVORITE_ITEM_FAB_POSITION = 0
    }

    private val stopDetailsVM: StopDetailsViewModel by viewModel()

    private val rate : Rate by inject()

    private val openLine: (LineDetailsFragmentArgs) -> Unit = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            findNavController().navigate(
                StopDetailsFragmentDirections.actionStopDetailsToLineDetails(
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
                    rate.showRequest(requireActivity().coordinator_layout, R.id.gap, requireContext())
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

        stop_details_no_data_animation?.visibility = newVisibility
        stop_details_no_data_text?.visibility = newVisibility
        stop_details_no_data_help?.visibility = newVisibility
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

        requireActivity().stop_details_fab.replaceActionItem(
            SpeedDialActionItem.Builder(R.id.stop_details_fab_favorite, newIcon)
                .setLabel(newLabel)
                .create(),
            FAVORITE_ITEM_FAB_POSITION
        )
    }

    private val noDataOnClickListener = View.OnClickListener {
        MaterialDialog(requireContext()).show {
            message(R.string.no_data_available_suggestions)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.stop_details_destination, container, false)
        setHasOptionsMenu(true)
        setupFab()
        rootView.favorites_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = stopDestinationsAdapter
        }

        rootView.stop_details_no_data_text.setOnClickListener(noDataOnClickListener)
        rootView.stop_details_no_data_help.setOnClickListener(noDataOnClickListener)
        rootView.swiperefresh.setOnRefreshListener {
            stopDetailsVM.refreshStopDestinations()
        }

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupToolbar()
        val args = StopDetailsFragmentArgs.fromBundle(requireArguments())
        stopDetailsVM.init(args.stopId, StopType.valueOf(args.stopType))

        stopDetailsVM.stopDestinations.observe(viewLifecycleOwner, stopDestinationsObserver)
        stopDetailsVM.refreshStopDestinations()
        stopDetailsVM.stopIsFavorited.observe(viewLifecycleOwner, stopFavoriteStatusObserver)
        stopDetailsVM.stopTitle.observe(viewLifecycleOwner, Observer { stopTitle ->
            (requireActivity() as MainActivity).setActionBarTitle(stopTitle)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        setupToolbar()
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setupToolbar() {
        requireActivity().main_toolbar?.let { toolbar ->
            (toolbar.menu.findItem(R.id.item_search)?.actionView as SearchView?)?.setOnQueryTextListener(null)
            toolbar.menu.clear()
            toolbar.inflateMenu(R.menu.stop_details_destination_menu)
        }
    }

    private fun setupFab() {
        requireActivity().stop_details_fab?.apply {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_refresh -> {
                stopDetailsVM.refreshStopDestinations()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
