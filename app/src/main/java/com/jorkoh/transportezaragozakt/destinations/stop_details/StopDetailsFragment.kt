package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build.VERSION.SDK_INT
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
import kotlinx.android.synthetic.main.stop_details_destination.*
import kotlinx.android.synthetic.main.stop_details_destination.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build.VERSION_CODES.O
import com.google.android.material.snackbar.Snackbar
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.input.input


class StopDetailsFragment : Fragment() {
    companion object : Serializable {
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

    private val onSwipeRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        stopDetailsVM.refreshStopDestinations()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.stop_details_destination, container, false)

        setupFab(rootView)

        GlobalScope.launch {
            delay(100)
            rootView.stop_details_fab_container.animate()
                .alpha(1f)
                .duration = 600
        }

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

        rootView.swiperefresh.setOnRefreshListener(onSwipeRefreshListener)

        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = StopDetailsFragmentArgs.fromBundle(requireArguments())
        stopDetailsVM.init(args.stopId, StopType.valueOf(args.stopType))
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

    private fun createReminder() {

    }

    private fun createShortcut(label: String) {
        if (SDK_INT >= O) {
            val shortcutManager = requireContext().getSystemService(ShortcutManager::class.java)
            val shortcut = ShortcutInfo.Builder(requireContext(), stopDetailsVM.stopID)
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
                .setIntent(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("launchTZ://viewStop/${stopDetailsVM.stopType.name}/${stopDetailsVM.stopID}/")
                    )
                ).build()
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

    private fun updateIsFavoritedUI(isFavorited: Boolean?, rootView: View) {
        val newIcon = if (isFavorited == true) {
            R.drawable.ic_favorite_black_24dp
        } else {
            R.drawable.ic_favorite_border_black_24dp
        }

        val newLabel = if (isFavorited == true) {
            R.string.fab_remove_favorite
        } else {
            R.string.fab_add_favorite
        }

        rootView.stop_details_fab.replaceActionItem(
            SpeedDialActionItem.Builder(R.id.stop_details_fab_favorite, newIcon)
                .setLabel(newLabel)
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
                stopDestinationsAdapter.setDestinations(stopDestinations.data.orEmpty(), stopDetailsVM.stopType)
                if (stopDestinations.data.isNullOrEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            Status.ERROR -> {
                rootView.swiperefresh?.isRefreshing = false
                stopDestinationsAdapter.setDestinations(listOf(), stopDetailsVM.stopType)
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

    private fun updateStopTitleUI(stopTitle: String?) {
        (requireActivity() as MainActivity).setActionBarTitle(stopTitle ?: "")
    }
}
