package com.jorkoh.transportezaragozakt.destinations.line_details

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
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
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.DEFAULT_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MAX_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MIN_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.ZARAGOZA_BOUNDS
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.ZARAGOZA_CENTER
import com.jorkoh.transportezaragozakt.destinations.map.MapViewModel
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.line_details_destination.*
import kotlinx.android.synthetic.main.line_details_destination.view.*
import kotlinx.android.synthetic.main.main_container.*
import kotlinx.android.synthetic.main.map_info_window.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LineDetailsFragment : Fragment() {

    private val mapVM: MapViewModel by sharedViewModel()
    private val lineDetailsVM: LineDetailsViewModel by sharedViewModel(from = { this })

    private lateinit var map: GoogleMap

    private val markers = mutableListOf<Marker>()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    //TODO CAN WE MAKE THIS NOT LATEINIT
    private lateinit var busMarkerIcon: BitmapDescriptor
    private lateinit var busFavoriteMarkerIcon: BitmapDescriptor
    private lateinit var tramMarkerIcon: BitmapDescriptor
    private lateinit var tramFavoriteMarkerIcon: BitmapDescriptor

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
            val markerOptions = MarkerOptions()
            markerOptions.position(stop.location)
            markerOptions.icon(
                when (stop.type) {
                    StopType.BUS -> if (stop.isFavorite) busFavoriteMarkerIcon else busMarkerIcon
                    StopType.TRAM -> if (stop.isFavorite) tramFavoriteMarkerIcon else tramMarkerIcon
                }
            )
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
            val selectedMarker = markers.find { marker ->
                (marker.tag as Stop).stopId == stopId
            }
            if (selectedMarker != null) {
                selectedMarker.showInfoWindow()
                map.animateCamera(CameraUpdateFactory.newLatLng((selectedMarker.tag as Stop).location))
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
        setupToolbar()
        val rootView = inflater.inflate(R.layout.line_details_destination, container, false)
        bottomSheetBehavior = BottomSheetBehavior.from(rootView.line_details_bottom_sheet)

        return rootView
    }

    private fun setupToolbar() {
        requireActivity().main_toolbar.menu.apply {
            (findItem(R.id.item_search)?.actionView as SearchView?)?.setOnQueryTextListener(null)
            clear()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mapVM.init()
        val args = LineDetailsFragmentArgs.fromBundle(requireArguments())
        lineDetailsVM.init(args.lineId, LineType.valueOf(args.lineType))
        if (!args.stopId.isNullOrEmpty()) {
            lineDetailsVM.selectedStopId.postValue(args.stopId)
        }

        var mapFragment =
            childFragmentManager.findFragmentByTag(getString(R.string.line_destination_map_fragment_tag)) as CustomSupportMapFragment?
        if (mapFragment == null) {
            mapFragment = CustomSupportMapFragment.newInstance(displayFilters = false, bottomMargin = true)
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
        map.setInfoWindowAdapter(StopInfoWindowAdapter())
        map.setOnMarkerClickListener { marker ->
            lineDetailsVM.selectedStopId.value = (marker.tag as Stop).stopId
            false
        }
        map.setOnInfoWindowCloseListener {
            lineDetailsVM.selectedStopId.value = ""
        }
        createBaseMarkers()

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

    internal inner class StopInfoWindowAdapter : GoogleMap.InfoWindowAdapter {
        override fun getInfoWindow(marker: Marker?): View? {
            return null
        }

        override fun getInfoContents(marker: Marker?): View? {
            if (marker == null) return null

            val stop = marker.tag as Stop
            val content = layoutInflater.inflate(R.layout.map_info_window, null)

            content.type_image_info_window.setImageResource(
                when (stop.type) {
                    StopType.BUS -> R.drawable.ic_bus
                    StopType.TRAM -> R.drawable.ic_tram
                }
            )

            //If the stopTitle is longer we can fit more lines while keeping a nice ratio
            content.lines_layout_favorite.columnCount = when {
                stop.stopTitle.length >= 24 -> 8
                stop.stopTitle.length >= 18 -> 6
                else -> 4
            }
            stop.lines.forEachIndexed { index, line ->
                layoutInflater.inflate(R.layout.map_info_window_line, content.lines_layout_favorite)
                val lineView = content.lines_layout_favorite.getChildAt(index) as TextView

                val lineColor = if (stop.type == StopType.BUS) R.color.bus_color else R.color.tram_color
                lineView.background.setColorFilter(
                    ContextCompat.getColor(requireContext(), lineColor),
                    PorterDuff.Mode.SRC_IN
                )
                lineView.text = line
            }
            content.number_text_info_window.text = stop.number
            content.title_text_info_window.text = stop.stopTitle

            return content
        }
    }

    private fun createBaseMarkers() {
        val busDrawable = resources.getDrawable(R.drawable.marker_bus, null) as BitmapDrawable
        val busBitmap = Bitmap.createScaledBitmap(
            busDrawable.bitmap,
            MapFragment.ICON_SIZE,
            MapFragment.ICON_SIZE,
            false
        )
        busMarkerIcon = BitmapDescriptorFactory.fromBitmap(busBitmap)

        val busFavoriteDrawable = resources.getDrawable(R.drawable.marker_bus_favorite, null) as BitmapDrawable
        val busFavoriteBitmap =
            Bitmap.createScaledBitmap(
                busFavoriteDrawable.bitmap,
                MapFragment.ICON_FAV_SIZE,
                MapFragment.ICON_FAV_SIZE,
                false
            )
        busFavoriteMarkerIcon = BitmapDescriptorFactory.fromBitmap(busFavoriteBitmap)


        val tramDrawable = resources.getDrawable(R.drawable.marker_tram, null) as BitmapDrawable
        val tramBitmap = Bitmap.createScaledBitmap(
            tramDrawable.bitmap,
            MapFragment.ICON_SIZE,
            MapFragment.ICON_SIZE,
            false
        )
        tramMarkerIcon = BitmapDescriptorFactory.fromBitmap(tramBitmap)

        val tramFavoriteDrawable = resources.getDrawable(R.drawable.marker_tram_favorite, null) as BitmapDrawable
        val tramFavoriteBitmap =
            Bitmap.createScaledBitmap(
                tramFavoriteDrawable.bitmap,
                MapFragment.ICON_FAV_SIZE,
                MapFragment.ICON_FAV_SIZE,
                false
            )
        tramFavoriteMarkerIcon = BitmapDescriptorFactory.fromBitmap(tramFavoriteBitmap)
    }
}