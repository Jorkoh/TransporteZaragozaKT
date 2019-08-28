package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.input.input
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.FragmentWithToolbar
import com.jorkoh.transportezaragozakt.destinations.createStopDetailsDeepLink
import com.jorkoh.transportezaragozakt.destinations.inflateLines
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


class StopDetailsFragment : FragmentWithToolbar() {
    companion object {
        const val FAVORITE_ITEM_FAB_POSITION = 0
    }

    private val stopDetailsVM: StopDetailsViewModel by viewModel()

    private val args: StopDetailsFragmentArgs by navArgs()

    private val rate: Rate by inject()
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val openLine: (LineDetailsFragmentArgs) -> Unit = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            findNavController().navigate(
                StopDetailsFragmentDirections.actionStopDetailsToLineDetails(
                    info.lineType,
                    info.lineId,
                    info.stopId
                )
            )
        }
    }

    private val stopDestinationsAdapter: StopDestinationsAdapter = StopDestinationsAdapter(openLine)

    private val stopDestinationsObserver = Observer<Resource<List<StopDestination>>> { stopDestinations ->
        val newVisibility = when (stopDestinations.status) {
            Status.SUCCESS -> {
                swiperefresh?.isRefreshing = false
                stopDestinationsAdapter.setDestinations(stopDestinations.data.orEmpty(), stopDetailsVM.stopType, stopDetailsVM.stopId)
                if (stopDestinations.data.isNullOrEmpty()) {
                    View.VISIBLE
                } else {
                    // Data loaded successfully, show the rate me snackbar if conditions are met
                    if (rate.showRequest(requireActivity().coordinator_layout, R.id.gap, requireContext())) {
                        // If it has been shown record it on Firebase
                        firebaseAnalytics.logEvent(getString(R.string.EVENT_RATE_ME_SHOWN), Bundle())
                    }
                    View.GONE
                }
            }
            Status.ERROR -> {
                swiperefresh?.isRefreshing = false
                stopDestinationsAdapter.setDestinations(listOf(), stopDetailsVM.stopType, stopDetailsVM.stopId)
                View.VISIBLE

            }
            Status.LOADING -> {
                swiperefresh?.isRefreshing = true
                stop_details_no_data_animation.visibility
            }
        }

        stop_details_no_data_animation.visibility = newVisibility
        stop_details_no_data_text.visibility = newVisibility
        stop_details_no_data_help.visibility = newVisibility
        // The scale is already stated in the layout but it occasionally gets ignored, it's related to the constraints
        // and the view inflating order but it's not a consistent bug
        stop_details_no_data_animation.scale = 0.3f
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
        return inflater.inflate(R.layout.stop_details_destination, container, false).apply {
            setupFab()
            stop_details_recycler_view.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = stopDestinationsAdapter
            }

            stop_details_no_data_text.setOnClickListener(noDataOnClickListener)
            stop_details_no_data_help.setOnClickListener(noDataOnClickListener)
            swiperefresh.setOnRefreshListener {
                stopDetailsVM.refreshStopDestinations()
            }
            setupToolbar(this)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        stopDetailsVM.init(args.stopId, StopType.valueOf(args.stopType))
        stopDetailsVM.stopDestinations.observe(viewLifecycleOwner, stopDestinationsObserver)
        stopDetailsVM.refreshStopDestinations()
        stopDetailsVM.stopIsFavorited.observe(viewLifecycleOwner, stopFavoriteStatusObserver)
        stopDetailsVM.stop.observe(viewLifecycleOwner, Observer { stop ->
            // Setup the toolbar
            type_image_stop_details.setImageResource(
                when (stop.type) {
                    StopType.BUS -> R.drawable.ic_bus
                    StopType.TRAM -> R.drawable.ic_tram
                }
            )
            fragment_toolbar.title = "${getString(R.string.stop)} ${stop.number}"
            stop_details_title_text_view.text = stop.stopTitle
            stop.lines.inflateLines(stop_details_lines_layout, stop.type, requireContext())
        })
    }

    private fun setupToolbar(rootView: View) {
        rootView.fragment_toolbar.apply {
            menu.clear()
            inflateMenu(R.menu.stop_details_destination_menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.item_refresh -> {
                        stopDetailsVM.refreshStopDestinations()
                        true
                    }
                    R.id.item_directions -> {
                        stopDetailsVM.stop.value?.location?.let { location ->
                            val mapIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("google.navigation:q=${location.latitude},${location.longitude}&mode=w")
                            )
                            mapIntent.setPackage("com.google.android.apps.maps")
                            mapIntent.resolveActivity(requireActivity().packageManager)?.run {
                                startActivity(mapIntent)
                            }
                        }
                        true
                    }
                    else -> super.onOptionsItemSelected(item)
                }
            }
        }
    }

    private fun setupFab() {
        requireActivity().stop_details_fab?.apply {
            // Items
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
            // Listeners
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
                                show24HoursView = DateFormat.is24HourFormat(requireContext()),
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
                            input(prefill = stopDetailsVM.stop.value?.stopTitle ?: "") { _, text ->
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
            if (shortcutManager.isRequestPinShortcutSupported) {
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
                val successCallback = PendingIntent.getBroadcast(
                    requireContext(),
                    0,
                    Intent(MainActivity.ACTION_SHORTCUT_PINNED),
                    0
                )
                shortcutManager.requestPinShortcut(shortcut, successCallback.intentSender)
            } else {
                Snackbar.make(
                    stop_details_fab,
                    R.string.shortcut_pinning_not_supported,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid leaks
        stop_details_recycler_view?.adapter = null
        activity?.stop_details_fab?.setOnActionSelectedListener(null)
    }
}
