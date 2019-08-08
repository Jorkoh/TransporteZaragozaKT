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
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.FragmentWithToolbar
import com.jorkoh.transportezaragozakt.destinations.map.CustomSupportMapFragment
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.DEFAULT_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MAX_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MIN_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.ZARAGOZA_BOUNDS
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.ZARAGOZA_CENTER
import com.jorkoh.transportezaragozakt.destinations.map.MapSettingsViewModel
import com.jorkoh.transportezaragozakt.destinations.map.MarkerIcons
import com.jorkoh.transportezaragozakt.destinations.map.StopInfoWindowAdapter
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

    private val markers = mutableListOf<Marker>()

    private val markerIcons: MarkerIcons by inject()

    @SuppressLint("MissingPermission")
    private val onMyLocationButtonClickListener = GoogleMap.OnMyLocationButtonClickListener {
        runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    if (ZARAGOZA_BOUNDS.contains(location.toLatLng())) {
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
        mapFragment.getMapAsync { map ->
            this.map = map
            setupMap(!ZARAGOZA_BOUNDS.contains(map.cameraPosition.target), mapFragment.viewLifecycleOwner)
        }
    }

    private fun setupMap(centerCamera: Boolean, mapLifecycleOwner: LifecycleOwner) {
        if (centerCamera) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    ZARAGOZA_CENTER,
                    DEFAULT_ZOOM
                )
            )
        }

        map.clear()

        styleMap()
        configureMap()
        setupObservers(mapLifecycleOwner)

        // Enable "My Location" layer
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

    private fun styleMap() {
        map.setMaxZoomPreference(MAX_ZOOM)
        map.setMinZoomPreference(MIN_ZOOM)
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.setLatLngBoundsForCameraTarget(ZARAGOZA_BOUNDS)
    }

    private fun configureMap() {
        map.setOnInfoWindowClickListener { marker ->
            val stop = marker.tag as Stop
            findNavController().navigate(
                LineDetailsFragmentDirections.actionLineDetailsToStopDetails(
                    stop.type.name,
                    stop.stopId
                )
            )
        }
        map.setInfoWindowAdapter(StopInfoWindowAdapter(requireContext()))
        map.setOnMarkerClickListener { marker ->
            lineDetailsVM.selectedStopId.postValue((marker.tag as Stop).stopId)
            false
        }
        map.setOnInfoWindowCloseListener {
            lineDetailsVM.selectedStopId.postValue("")
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
                        if (lineDetailsVM.lineType == LineType.BUS) R.color.bus_color else R.color.tram_color
                    )
                )
            val bounds = LatLngBounds.builder()
            locations.forEach {
                line.add(it.location)
                bounds.include(it.location)
            }
            map.addPolyline(line)
            // Only adjust the camera to reveal the entire line when user didn't come from a specific stop and
            // the camera hasn't already moved
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
                fragment_toolbar.title = "${getString(R.string.line)} ${line.name}"
                // Load the bottom sheet with the stops by destination
                line_details_viewpager.adapter = StopDestinationsPagerAdapter(
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
                            (marker.tag as Stop).stopId == stopId
                        }?.let { selectedMarker ->
                            selectedMarker.showInfoWindow()
                            map.animateCamera(CameraUpdateFactory.newLatLng((selectedMarker.tag as Stop).location))
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
            val markerOptions = MarkerOptions().apply {
                position(stop.location)
                icon(
                    when (stop.type) {
                        StopType.BUS -> if (stop.isFavorite) markerIcons.favoriteBus else markerIcons.normalBus
                        StopType.TRAM -> if (stop.isFavorite) markerIcons.favoriteTram else markerIcons.normalTram
                    }
                )
            }
            val marker = map.addMarker(markerOptions)
            marker.tag = stop
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
                                    title = getString(R.string.bus_line_schedule_title).replace("%", lineDetailsVM.lineId),
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
        // Clearing the markers activates this listener so it has to be unregistered to avoid issues when the map fragment is reused
        map.setOnInfoWindowCloseListener(null)
        super.onDestroyView()
    }
}