package com.jorkoh.transportezaragozakt.destinations.line_details

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.SphericalUtil
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.LineType
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.destinations.FragmentWithToolbar
import com.jorkoh.transportezaragozakt.destinations.isSpanish
import com.jorkoh.transportezaragozakt.destinations.map.*
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.DEFAULT_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MAX_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MIN_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MIN_ZOOM_RURAL
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.RURAL_BOUNDS
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.ZARAGOZA_BOUNDS
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.ZARAGOZA_CENTER
import com.jorkoh.transportezaragozakt.destinations.officialLineIdToBusWebLineId
import com.jorkoh.transportezaragozakt.destinations.toLatLng
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.line_details_destination.*
import kotlinx.android.synthetic.main.line_details_destination.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LineDetailsFragment : FragmentWithToolbar() {

    private val lineDetailsVM: LineDetailsViewModel by sharedViewModel(from = { this })
    private val mapSettingsVM: MapSettingsViewModel by sharedViewModel()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    var ACTIVE_MIN_ZOOM = MIN_ZOOM
    var ACTIVE_BOUNDS: LatLngBounds = RURAL_BOUNDS

    private val markers = mutableListOf<Marker>()

    private val markerIcons: MarkerIcons by inject()

    @SuppressLint("MissingPermission")
    private val onMyLocationButtonClickListener = GoogleMap.OnMyLocationButtonClickListener {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                if (ACTIVE_BOUNDS.contains(location.toLatLng())) {
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

        val args = LineDetailsFragmentArgs.fromBundle(requireArguments())
        lineDetailsVM.init(args.lineId, LineType.valueOf(args.lineType))
        // If the user navigated from a specific stop select it on the map
        if (!args.stopId.isNullOrEmpty()) {
            lineDetailsVM.selectedStopId.postValue(args.stopId)
            // Since this should only happen the first time let's clear the argument
            requireArguments().putString("stopId", null)
        }

        var mapFragment =
            childFragmentManager.findFragmentByTag(getString(R.string.line_destination_map_fragment_tag)) as CustomSupportMapFragment?
        if (mapFragment == null) {
            mapFragment = CustomSupportMapFragment.newInstance(
                displayFilters = false,
                bottomPaddingDimen = R.dimen.line_details_destination_map_view_bottom_padding
            )
            childFragmentManager.beginTransaction()
                .add(R.id.map_fragment_container_line, mapFragment, getString(R.string.line_destination_map_fragment_tag))
                .commit()
        }
        mapFragment.getMapAsync { newMap ->
            val mapNeedsSetup = !::map.isInitialized
            val cameraNeedsCentering = !ACTIVE_BOUNDS.contains(newMap.cameraPosition.target)
            this.map = newMap

            if (mapNeedsSetup) {
                setupMap(cameraNeedsCentering)
            }

            with(mapFragment.viewLifecycleOwner) {
                if (lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                    setupObservers(this)
                }
            }
            enableLocationLayer(cameraNeedsCentering)
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
        map.setMinZoomPreference(ACTIVE_MIN_ZOOM)
        map.setLatLngBoundsForCameraTarget(ACTIVE_BOUNDS)
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
    }

    private fun configureMap() {
        map.setOnInfoWindowClickListener { marker ->
            val stop = requireNotNull((marker.tag as CustomClusterItem).stop)
            findNavController().navigate(
                LineDetailsFragmentDirections.actionLineDetailsToStopDetails(
                    stop.type.name,
                    stop.stopId
                )
            )
        }
        map.setInfoWindowAdapter(CustomInfoWindowAdapter(requireContext()))
        map.setOnMarkerClickListener { marker ->
            lineDetailsVM.selectedStopId.postValue((marker.tag as CustomClusterItem).stop?.stopId)
            false
        }
        map.setOnInfoWindowCloseListener {
            lineDetailsVM.selectedStopId.postValue("")
        }
    }

    private fun enableLocationLayer(cameraNeedsCentering: Boolean) {
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
            val line = PolylineOptions()
                .clickable(false)
                .color(
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
                line.add(it.location)
                bounds.include(it.location)
            }
            map.addPolyline(line)
            // Only adjust the camera to reveal the entire line when user didn't come from a specific stop and the camera hasn't already moved
            if (lineDetailsVM.selectedStopId.value.isNullOrEmpty()
                && SphericalUtil.computeDistanceBetween(map.cameraPosition.target, ZARAGOZA_CENTER) < 1
            ) {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 75))
            }
        })

        // Line itself
        lineDetailsVM.line.observe(viewLifecycleOwner, Observer { line ->
            line?.let {
                // Set the action bar title
                fragment_toolbar.title = getString(R.string.line_template, if (requireContext().isSpanish()) line.nameES else line.nameEN)
                // Map bounds and min  depend on line type
                ACTIVE_BOUNDS = if (line.type == LineType.RURAL) RURAL_BOUNDS else ZARAGOZA_BOUNDS
                map.setLatLngBoundsForCameraTarget(ACTIVE_BOUNDS)
                ACTIVE_MIN_ZOOM = if (line.type == LineType.RURAL) MIN_ZOOM_RURAL else MIN_ZOOM
                map.setMinZoomPreference(ACTIVE_MIN_ZOOM)
                // Load the bottom sheet with the stops by destination
                line_details_viewpager.adapter = StopsByDestinationPagerAdapter(
                    childFragmentManager,
                    requireNotNull(lineDetailsVM.line.value)
                )
                line_details_tab_layout.setupWithViewPager(line_details_viewpager)
            }
        })

        // Line stops
        lineDetailsVM.stops.observe(mapLifecycleOwner, Observer { stops ->
            stops?.let {
                // Get rid of the old markers and set up new markers
                renewMarkers(stops)

                // Setup the observer for the selected stop, this can't be done until markers are ready
                lineDetailsVM.selectedStopId.observe(viewLifecycleOwner, Observer { stopId ->
                    if (!stopId.isNullOrEmpty()) {
                        markers.find { marker ->
                            (marker.tag as CustomClusterItem).stop?.stopId == stopId
                        }?.let { selectedMarker ->
                            selectedMarker.showInfoWindow()
                            map.animateCamera(
                                CameraUpdateFactory.newLatLng((selectedMarker.tag as CustomClusterItem).stop?.location),
                                240,
                                null
                            )
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                })
            }
        })
    }

    private fun renewMarkers(stops: List<Stop>) {
        markers.forEach { it.remove() }
        markers.clear()
        stops.forEach { stop ->
            val item = CustomClusterItem(stop)
            val markerOptions = MarkerOptions().apply {
                position(stop.location)
                icon(item.type.getMarkerIcon(markerIcons))
            }
            val marker = map.addMarker(markerOptions)
            marker.tag = item
            markers.add(marker)
        }
    }

    private fun setupToolbar(rootView: View) {
        rootView.fragment_toolbar.apply {
            menu.clear()
            inflateMenu(R.menu.line_details_destination_menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.item_schedule -> {
                        if (lineDetailsVM.lineType == LineType.TRAM) {
                            findNavController().navigate(
                                LineDetailsFragmentDirections.actionLineDetailsToWebView(
                                    url = getString(R.string.tram_line_schedule_url),
                                    title = getString(R.string.tram_line_schedule_title),
                                    javascript = getString(R.string.tram_line_schedule_javascript)
                                )
                            )
                        } else {
                            findNavController().navigate(
                                LineDetailsFragmentDirections.actionLineDetailsToWebView(
                                    url = getString(R.string.bus_line_schedule_url).replace(
                                        "%",
                                        lineDetailsVM.lineId.officialLineIdToBusWebLineId()
                                    ),
                                    title = getString(R.string.bus_line_schedule_title, lineDetailsVM.lineId),
                                    javascript = getString(R.string.bus_line_schedule_javascript)
                                )
                            )
                        }
                        true
                    }
                    else -> super.onOptionsItemSelected(item)
                }
            }
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
    }
}