package com.jorkoh.transportezaragozakt.destinations.line_details

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.SphericalUtil
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.destinations.map.CustomSupportMapFragment
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.DEFAULT_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MAX_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MIN_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.ZARAGOZA_BOUNDS
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.ZARAGOZA_CENTER
import com.jorkoh.transportezaragozakt.destinations.map.MapViewModel
import com.jorkoh.transportezaragozakt.destinations.map.MarkerIcons
import com.jorkoh.transportezaragozakt.destinations.map.StopInfoWindowAdapter
import com.jorkoh.transportezaragozakt.destinations.toPx
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.line_details_destination.*
import kotlinx.android.synthetic.main.line_details_destination.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LineDetailsFragment : Fragment() {

    private val mapVM: MapViewModel by sharedViewModel()
    private val lineDetailsVM: LineDetailsViewModel by sharedViewModel(from = { this })

    private lateinit var map: GoogleMap

    private val markers = mutableListOf<Marker>()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    private val markerIcons: MarkerIcons by inject()

    private val lineLocationsObserver = Observer<List<LineLocation>> { locations ->
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
        // Only adjust the camera to reveal the entire line when user didn't come from a specific stop
        if (lineDetailsVM.selectedStopId.value.isNullOrEmpty()) {
            if (SphericalUtil.computeDistanceBetween(map.cameraPosition.target, ZARAGOZA_CENTER) < 1) {
                map.setPadding(0, 0, 0, 160.toPx())
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 75))
                map.setPadding(0, 0, 0, 0)
            }
        }
    }

    private val lineStopsObserver = Observer<List<Stop>> { stops ->
        markers.forEach {
            it.remove()
        }
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

        line_details_viewpager.adapter = StopDestinationsPagerAdapter(
            childFragmentManager,
            requireNotNull(lineDetailsVM.line.value)
        )
        line_details_tab_layout.setupWithViewPager(line_details_viewpager)
        lineDetailsVM.selectedStopId.observe(viewLifecycleOwner, Observer { stopId ->
            if (!stopId.isNullOrEmpty()) {
                val selectedMarker = markers.find { marker ->
                    (marker.tag as Stop).stopId == stopId
                }
                if (selectedMarker != null) {
                    selectedMarker.showInfoWindow()
                    map.animateCamera(CameraUpdateFactory.newLatLng((selectedMarker.tag as Stop).location))
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        })
    }

    private val lineObserver = Observer<Line> { line ->
        (requireActivity() as MainActivity).setActionBarTitle("${getString(R.string.line)} ${line.name}")
        //TODO THIS SHOULD HAVE BEEN DONE IN THE VM
        lineDetailsVM.loadStops(line.stopIdsFirstDestination + line.stopIdsSecondDestination)
        lineDetailsVM.stops.observe(viewLifecycleOwner, lineStopsObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.line_details_destination, container, false).also { rootView ->
            bottomSheetBehavior = BottomSheetBehavior.from(rootView.line_details_bottom_sheet)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val args = LineDetailsFragmentArgs.fromBundle(requireArguments())
        lineDetailsVM.init(args.lineId, LineType.valueOf(args.lineType))
        if (!args.stopId.isNullOrEmpty()) {
            lineDetailsVM.selectedStopId.postValue(args.stopId)
        }

        var mapFragment =
            childFragmentManager.findFragmentByTag(getString(R.string.line_destination_map_fragment_tag)) as CustomSupportMapFragment?
        if (mapFragment == null) {
            mapFragment = CustomSupportMapFragment.newInstance(displayFilters = false, bottomMargin = 160)
            childFragmentManager.beginTransaction()
                .add(
                    R.id.map_fragment_container_line,
                    mapFragment,
                    getString(R.string.line_destination_map_fragment_tag)
                )
                .commit()
            childFragmentManager.executePendingTransactions()
        }
        mapFragment.getMapAsync { map ->
            setupMap(map, !ZARAGOZA_BOUNDS.contains(map.cameraPosition.target))
            mapVM.isDarkMap.observe(viewLifecycleOwner, Observer { isDarkMap ->
                map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        context,
                        if (isDarkMap) R.raw.map_style_dark else R.raw.map_style
                    )
                )
            })
        }
    }

    private fun setupMap(googleMap: GoogleMap?, centerCamera: Boolean) {
        map = checkNotNull(googleMap)

        if (centerCamera) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    ZARAGOZA_CENTER,
                    DEFAULT_ZOOM
                )
            )
        }

        // Setting this to null while clearing old markers to avoid triggering
        // the listener that reacts to the user performing the action
        map.setOnInfoWindowCloseListener(null)
        map.clear()
        styleMap()

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

        mapVM.mapType.observe(viewLifecycleOwner, Observer { mapType ->
            map.mapType = mapType
        })
        mapVM.trafficEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            map.isTrafficEnabled = enabled
        })
        lineDetailsVM.lineLocations.observe(viewLifecycleOwner, lineLocationsObserver)
        lineDetailsVM.line.observe(viewLifecycleOwner, lineObserver)

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
}