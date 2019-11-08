package com.jorkoh.transportezaragozakt.destinations.line_details

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.maps.android.SphericalUtil
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.LineType
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.destinations.*
import com.jorkoh.transportezaragozakt.destinations.map.*
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.DEFAULT_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MAX_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MIN_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MIN_ZOOM_RURAL
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.RURAL_BOUNDS
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.ZARAGOZA_BOUNDS
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.ZARAGOZA_CENTER
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.line_details_destination.*
import kotlinx.android.synthetic.main.line_details_destination.view.*
import kotlinx.android.synthetic.main.map_trackings_control.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.core.parameter.parametersOf
import java.text.DateFormat

class LineDetailsFragment : FragmentWithToolbar() {

    private val args: LineDetailsFragmentArgs by navArgs()

    private val lineDetailsVM: LineDetailsViewModel by sharedViewModel(from = { this }) {
        parametersOf(
            args.lineId,
            LineType.valueOf(args.lineType)
        )
    }
    private val mapSettingsVM: MapSettingsViewModel by sharedViewModel()

    private var activeMinZoom = MIN_ZOOM
    private var activeBounds: LatLngBounds = RURAL_BOUNDS

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>? = null

    private val stopMarkers = mutableListOf<Marker>()
    private val trackingMarkers = mutableListOf<Marker>()

    private val markerIcons: MarkerIcons by inject()

    private val selectTracking: (RuralTracking) -> Unit = { tracking ->
        trackingsDialog?.dismiss()
        lifecycleScope.launchWhenStarted {
            lineDetailsVM.selectedItemId.send(tracking.vehicleId)
        }
    }

    private var trackingsDialog: MaterialDialog? = null
    private val trackingsAdapter = TrackingsAdapter(selectTracking)

    //TODO Contact CTAZ to figure out a realistic way to keep this updated
    val ruralLinesAndScheduleURLs: Map<String, String> = mapOf(
        Pair("101", "http://www.consorciozaragoza.es/sites/default/files/101_201803_0.pdf"),
        Pair("102", "http://www.consorciozaragoza.es/sites/default/files/102_201803_0.pdf"),
        Pair("201", "http://www.consorciozaragoza.es/sites/default/files/201_201807_0.pdf"),
        Pair("501", "http://www.consorciozaragoza.es/sites/default/files/501_201902.pdf"),
        Pair("601", "http://www.consorciozaragoza.es/sites/default/files/601_201801.pdf"),
        Pair("602", "http://www.consorciozaragoza.es/sites/default/files/602_20170113_0.pdf"),
        Pair("603", "http://www.consorciozaragoza.es/sites/default/files/603_201803_0.pdf"),
        Pair("604", "http://www.consorciozaragoza.es/sites/default/files/604_201803_0.pdf"),
        Pair("605", "http://www.consorciozaragoza.es/sites/default/files/605_201803.pdf")
    )

    @SuppressLint("MissingPermission")
    private val onMyLocationButtonClickListener = GoogleMap.OnMyLocationButtonClickListener {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                if (activeBounds.contains(location.toLatLng())) {
                    //Smoothly pan the user towards their position, reset the zoom level and bearing
                    val cameraPosition = CameraPosition.builder()
                        .target(location.toLatLng())
                        .zoom(DEFAULT_ZOOM)
                        .bearing(0f)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                } else {
                    (requireActivity() as MainActivity).makeSnackbar(getString(R.string.location_outside_zaragoza_bounds))
                }
            }
        }
        true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.line_details_destination, container, false).apply {
            bottomSheetBehavior = BottomSheetBehavior.from(line_details_bottom_sheet)
            setupToolbar(this)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        var mapFragment =
            childFragmentManager.findFragmentByTag(getString(R.string.line_destination_map_fragment_tag)) as CustomSupportMapFragment?
        if (mapFragment == null) {
            mapFragment = CustomSupportMapFragment.newInstance(
                displayFilters = false,
                displayTrackingsButton = true,
                bottomPaddingDimen = R.dimen.line_details_destination_map_view_bottom_padding
            )
            childFragmentManager.beginTransaction()
                .add(R.id.map_fragment_container_line, mapFragment, getString(R.string.line_destination_map_fragment_tag))
                .commit()
        }
        mapFragment.getMapAsync { newMap ->
            val mapNeedsSetup = !::map.isInitialized
            val cameraNeedsCentering = !activeBounds.contains(newMap.cameraPosition.target)
            this.map = newMap

            if (mapNeedsSetup) {
                setupMap(cameraNeedsCentering)
            }

            with(mapFragment.viewLifecycleOwner) {
                if (lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                    setupObservers(this)
                }
            }
            enableLocationLayer()

            if (lineDetailsVM.lineType == LineType.RURAL) {
                map_trackings_button.visibility = View.VISIBLE
                map_trackings_button.setOnClickListener(DebounceClickListener {
                    if (trackingMarkers.size > 0) {
                        // Display a dialog to select the tracking
                        trackingsDialog = MaterialDialog(requireContext()).show {
                            title(text = getTrackingsTitle())
                            cancelOnTouchOutside(true)
                            customListAdapter(trackingsAdapter)
                        }
                        updateTrackingsDialogDistance()
                    } else {
                        (requireActivity() as MainActivity).makeSnackbar(getString(R.string.no_trackings_available))
                    }
                })
            }
        }
    }

    private fun setupMap(centerCamera: Boolean) {
        if (centerCamera) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    ZARAGOZA_CENTER,
                    DEFAULT_ZOOM
                )
            )
        }
        configureMap()
        styleMap()
    }

    private fun styleMap() {
        map.setMaxZoomPreference(MAX_ZOOM)
        map.setMinZoomPreference(activeMinZoom)
        map.setLatLngBoundsForCameraTarget(activeBounds)
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
    }

    private fun configureMap() {
        map.setOnInfoWindowClickListener { marker ->
            (marker.tag as CustomClusterItem).stop?.let { stop ->
                findNavController().navigate(
                    LineDetailsFragmentDirections.actionLineDetailsToStopDetails(
                        stop.type.name,
                        stop.stopId
                    )
                )
            }
        }
        map.setInfoWindowAdapter(CustomInfoWindowAdapter(requireContext()))
        map.setOnMarkerClickListener { marker ->
            lineDetailsVM.preservedItemId.postValue((marker.tag as CustomClusterItem).itemId)
            false
        }

        map.setOnInfoWindowCloseListener {
            lineDetailsVM.preservedItemId.postValue("")
        }
    }

    private fun enableLocationLayer() {
        runWithPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            options = QuickPermissionsOptions(
                handleRationale = true,
                rationaleMessage = getString(R.string.location_rationale),
                handlePermanentlyDenied = false
            )
        ) {
            @SuppressLint("MissingPermission")
            map.isMyLocationEnabled = true
            map.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener)
        }
    }

    private fun setupObservers(mapLifecycleOwner: LifecycleOwner) {
        // Map style
        mapSettingsVM.mapType.observe(mapLifecycleOwner, Observer { mapType ->
            map.mapType = mapType
        })
        mapSettingsVM.trafficEnabled.observe(mapLifecycleOwner, Observer { enabled ->
            map.isTrafficEnabled = enabled
        })
        mapSettingsVM.isDarkMap.observe(mapLifecycleOwner, Observer { isDarkMap ->
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, if (isDarkMap) R.raw.map_style_dark else R.raw.map_style))
        })

        // Line route locations
        lineDetailsVM.lineLocations.observe(mapLifecycleOwner, Observer { locations ->
            lifecycleScope.launch {
                val line = withContext(Dispatchers.Default) {
                    PolylineOptions().apply {
                        clickable(false)
                        color(
                            ContextCompat.getColor(
                                requireContext(),
                                when (lineDetailsVM.lineType) {
                                    LineType.BUS -> R.color.bus_color
                                    LineType.TRAM -> R.color.tram_color
                                    LineType.RURAL -> R.color.rural_color
                                }
                            )
                        )
                        val bounds = LatLngBounds.builder()
                        locations.forEach {
                            add(it.location)
                            bounds.include(it.location)
                        }
                    }
                }
                map.addPolyline(line)

                val bounds = withContext(Dispatchers.Default) {
                    LatLngBounds.builder().apply {
                        locations.forEach {
                            include(it.location)
                        }
                    }
                }
                // Only adjust the camera to reveal the entire line when user didn't come from a specific stop and the camera hasn't already moved
                if (lineDetailsVM.preservedItemId.value.isNullOrEmpty() && SphericalUtil.computeDistanceBetween(
                        map.cameraPosition.target,
                        ZARAGOZA_CENTER
                    ) < 1
                ) {
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 75))
                }
            }
        })

        // Line itself
        lineDetailsVM.line.observe(viewLifecycleOwner, Observer { line ->
            if (line != null) {
                // Set the action bar title
                fragment_toolbar.title = getString(R.string.line_template, if (requireContext().isSpanish()) line.nameES else line.nameEN)
                // Map bounds and min  depend on line type
                activeBounds = if (line.type == LineType.RURAL) RURAL_BOUNDS else ZARAGOZA_BOUNDS
                map.setLatLngBoundsForCameraTarget(activeBounds)
                activeMinZoom = if (line.type == LineType.RURAL) MIN_ZOOM_RURAL else MIN_ZOOM
                map.setMinZoomPreference(activeMinZoom)
                // Load the bottom sheet with the stops by destination
                line_details_viewpager.adapter = StopsByDestinationPagerAdapter(
                    childFragmentManager,
                    requireNotNull(lineDetailsVM.line.value)
                )
                line_details_tab_layout.setupWithViewPager(line_details_viewpager)
            } else {
                (requireActivity() as MainActivity).makeSnackbar(getString(R.string.line_not_found))
                FirebaseAnalytics.getInstance(requireContext()).logEvent("LINE_NOT_FOUND", Bundle().apply {
                    putString("LINE_ID", lineDetailsVM.lineId)
                })
                findNavController().popBackStack()
            }
        })

        // Line stops
        lineDetailsVM.stops.observe(mapLifecycleOwner, Observer { stops ->
            stops?.let {
                // Get rid of the old markers and set up new markers
                renewStopMarkers(stops)
            }
        })

        // Trackings
        lineDetailsVM.ruralTrackings?.observe(mapLifecycleOwner, Observer { trackings ->
            trackings?.let {
                renewTrackingMarkers(trackings)
                // Update the trackings selector
                trackingsAdapter.setNewTrackings(trackings)
                trackingsDialog?.title(text = getTrackingsTitle())
                updateTrackingsDialogDistance()
            }
        })

        lifecycleScope.launchWhenStarted {
            // When a new item is selected (through bottom sheet or tracking position update) recenter to it
            for (selectedItemId in lineDetailsVM.selectedItemId) {
                findMarker(selectedItemId)?.run {
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                    map.animateCamera(CameraUpdateFactory.newLatLng((tag as CustomClusterItem).position), 240, null)
                    showInfoWindow()
                    lineDetailsVM.preservedItemId.postValue(selectedItemId)
                }
            }
        }

        // If the user navigated from a specific stop select it on the map
        lifecycleScope.launchWhenStarted {
            args.stopId?.let {
                lineDetailsVM.selectedItemId.send(it)
            }
            // Since this should only happen the first time let's clear the argument
            requireArguments().putString("stopId", null)
        }
    }

    private fun renewStopMarkers(newStops: List<Stop>) {
        val preservedItemId = lineDetailsVM.preservedItemId.value

        stopMarkers.forEach { it.remove() }
        stopMarkers.clear()
        newStops.forEach { stop ->
            val item = CustomClusterItem(stop)
            val markerOptions = MarkerOptions().apply {
                position(stop.location)
                icon(item.type.getMarkerIcon(markerIcons))
            }
            val marker = map.addMarker(markerOptions)
            marker.tag = item
            stopMarkers.add(marker)
        }

        if (preservedItemId != null && newStops.any { it.stopId == preservedItemId }) {
            findMarker(preservedItemId)?.showInfoWindow()
            lineDetailsVM.preservedItemId.postValue(preservedItemId)
        }
    }

    private fun renewTrackingMarkers(newTrackings: List<RuralTracking>) {
        val preservedItemId = lineDetailsVM.preservedItemId.value
        val oldMarker = findMarker(preservedItemId)

        trackingMarkers.forEach { it.remove() }
        trackingMarkers.clear()
        newTrackings.forEach { tracking ->
            val item = CustomClusterItem(tracking)
            val markerOptions = MarkerOptions().apply {
                position(tracking.location)
                icon(item.type.getMarkerIcon(markerIcons))
            }
            val marker = map.addMarker(markerOptions)
            marker.tag = item
            trackingMarkers.add(marker)
        }

        val newTracking = newTrackings.firstOrNull { it.vehicleId == preservedItemId }
        if (preservedItemId != null && newTracking != null) {
            // If the selected item is a tracking that exists after the renewal, it has changed position
            // and the previous one was on the screen let's recenter to it
            if (oldMarker != null
                && SphericalUtil.computeDistanceBetween(oldMarker.position, newTracking.location) > 1
                && map.projection.visibleRegion.latLngBounds.contains(oldMarker.position)
            ) {
                lifecycleScope.launchWhenStarted {
                    lineDetailsVM.selectedItemId.send(preservedItemId)
                }
            } else {
                findMarker(preservedItemId)?.showInfoWindow()
                lineDetailsVM.preservedItemId.postValue(preservedItemId)
            }
        }
    }

    private fun findMarker(itemId: String?): Marker? =
        if (itemId == null) {
            null
        } else {
            stopMarkers.plus(trackingMarkers).find { marker ->
                (marker.tag as CustomClusterItem).itemId == itemId
            }
        }

    private fun setupToolbar(rootView: View) {
        rootView.fragment_toolbar.apply {
            menu.clear()
            inflateMenu(R.menu.line_details_destination_menu)
            setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.item_schedule) {
                    openSchedule()
                    true
                } else {
                    super.onOptionsItemSelected(item)
                }
            }
        }
    }

    private fun openSchedule() {
        when (lineDetailsVM.lineType) {
            LineType.BUS -> {
                findNavController().navigate(
                    LineDetailsFragmentDirections.actionLineDetailsToWebView(
                        url = getString(R.string.bus_line_schedule_url, lineDetailsVM.lineId.officialLineIdToBusWebLineId()),
                        title = getString(R.string.bus_line_schedule_title, lineDetailsVM.lineId),
                        javascript = getString(R.string.bus_line_schedule_javascript)
                    )
                )
            }
            LineType.TRAM -> {
                findNavController().navigate(
                    LineDetailsFragmentDirections.actionLineDetailsToWebView(
                        url = getString(R.string.tram_line_schedule_url),
                        title = getString(R.string.tram_line_schedule_title),
                        javascript = getString(R.string.tram_line_schedule_javascript)
                    )
                )
            }
            LineType.RURAL -> {
                if (ruralLinesAndScheduleURLs.containsKey(lineDetailsVM.lineId)) {
                    val scheduleIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(
                            Uri.parse(ruralLinesAndScheduleURLs[lineDetailsVM.lineId]),
                            "application/pdf"
                        )
                    }
                    startActivity(Intent.createChooser(scheduleIntent, getString(R.string.rural_line_schedule_title, lineDetailsVM.lineId)))
                }
            }
        }
    }

    private fun getTrackingsTitle(): String {
        val time = (trackingMarkers.getOrNull(0)?.tag as CustomClusterItem?)?.ruralTracking?.updatedAt
        return if (time != null) {
            getString(R.string.rural_trackings_template, DateFormat.getTimeInstance(DateFormat.SHORT).format(time))
        } else {
            getString(R.string.rural_trackings)
        }
    }

    private fun updateTrackingsDialogDistance() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let { trackingsAdapter.setNewLocation(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (::map.isInitialized) {
            // Clearing the markers activates this listener so it has to be unregistered to avoid issues when the map fragment is reused
            map.setOnInfoWindowCloseListener(null)
            // Avoid leaks
            @SuppressLint("MissingPermission")
            map.isMyLocationEnabled = false
        }

        // Avoid leaks
        bottomSheetBehavior = null
    }
}