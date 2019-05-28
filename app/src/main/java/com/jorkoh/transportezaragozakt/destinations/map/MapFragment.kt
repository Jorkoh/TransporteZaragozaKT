package com.jorkoh.transportezaragozakt.destinations.map

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.repositories.util.Status
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.main_container.*
import kotlinx.android.synthetic.main.map_info_window.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MapFragment : Fragment() {

    companion object {
        const val ICON_SIZE = 55
        const val ICON_FAV_SIZE = 70
        const val MAX_ZOOM = 17.5f
        const val MIN_ZOOM = 12f
        const val DEFAULT_ZOOM = 16f
        const val MAX_CLUSTERING_ZOOM = 15.5f
        val ZARAGOZA_BOUNDS = LatLngBounds(
            LatLng(41.6000, -1.08125), LatLng(41.774594, -0.7933)
        )
        val ZARAGOZA_CENTER = LatLng(41.656362, -0.878920)
    }

    private val mapVM: MapViewModel by sharedViewModel()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<Stop>
    private lateinit var map: GoogleMap
    private lateinit var clickedClusterItem: Stop

    private lateinit var busMarkerIcon: BitmapDescriptor
    private lateinit var busFavoriteMarkerIcon: BitmapDescriptor
    private lateinit var tramMarkerIcon: BitmapDescriptor
    private lateinit var tramFavoriteMarkerIcon: BitmapDescriptor

    private val busStops = mutableListOf<Stop>()
    private val tramStops = mutableListOf<Stop>()

    private val busStopLocationsObserver = Observer<List<Stop>> { stops ->
        if (mapVM.busFilterEnabled.value == true) {
            stops.forEach { clusterManager.removeItem(it) }
            clusterManager.addItems(stops)
            clusterManager.cluster()
        }
        busStops.clear()
        busStops.addAll(stops)
    }

    private val tramStopLocationsObserver = Observer<List<Stop>> { stops ->
        if (mapVM.tramFilterEnabled.value == true) {
            stops.forEach { clusterManager.removeItem(it) }
            clusterManager.addItems(stops)
            clusterManager.cluster()
        }
        tramStops.clear()
        tramStops.addAll(stops)
    }

    private val busFilterEnabledObserver = Observer<Boolean> { enabled ->
        if (enabled) {
            clusterManager.addItems(busStops)
        } else {
            busStops.forEach { clusterManager.removeItem(it) }
        }
        clusterManager.cluster()
    }

    private val tramFilterEnabledObserver = Observer<Boolean> { enabled ->
        if (enabled) {
            clusterManager.addItems(tramStops)
        } else {
            tramStops.forEach { clusterManager.removeItem(it) }
        }
        clusterManager.cluster()
    }

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
                    }else{
                        (requireActivity() as MainActivity).makeSnackbar(getString(R.string.location_outside_zaragoza_bounds))
                    }
                }
            }
        }
        true
    }

    internal inner class StopInfoWindowAdapter : GoogleMap.InfoWindowAdapter {
        override fun getInfoWindow(marker: Marker?): View? {
            return null
        }

        override fun getInfoContents(marker: Marker?): View? {
            if (marker == null) return null

            val stop = clickedClusterItem
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupToolbar()
        return inflater.inflate(R.layout.map_destination, container, false)
    }

    private fun setupToolbar() {
        requireActivity().main_toolbar.menu.apply {
            (findItem(R.id.item_search)?.actionView as SearchView?)?.setOnQueryTextListener(null)
            clear()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mapVM.init()

        var mapFragment =
            childFragmentManager.findFragmentByTag(getString(R.string.map_destination_map_fragment_tag)) as CustomSupportMapFragment?
        if (mapFragment == null) {
            mapFragment = CustomSupportMapFragment.newInstance(true, false)
            childFragmentManager.beginTransaction()
                .add(R.id.map_fragment_container, mapFragment, getString(R.string.map_destination_map_fragment_tag))
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
        busStops.clear()
        tramStops.clear()
        styleMap()
        setupClusterManager()

        mapVM.mapType.observe(viewLifecycleOwner, Observer { mapType ->
            map.mapType = mapType
        })
        mapVM.trafficEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            map.isTrafficEnabled = enabled
        })
        mapVM.busFilterEnabled.observe(viewLifecycleOwner, busFilterEnabledObserver)
        mapVM.tramFilterEnabled.observe(viewLifecycleOwner, tramFilterEnabledObserver)
        mapVM.getBusStopLocations().observe(viewLifecycleOwner, busStopLocationsObserver)
        mapVM.getTramStopLocations().observe(viewLifecycleOwner, tramStopLocationsObserver)

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
            if (centerCamera) {
                onMyLocationButtonClickListener.onMyLocationButtonClick()
            }
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

    private fun setupClusterManager() {
        clusterManager = ClusterManager(context, map)

        map.setOnMarkerClickListener(clusterManager)
        map.setInfoWindowAdapter(clusterManager.markerManager)
        map.setOnInfoWindowClickListener(clusterManager)
        map.setOnCameraIdleListener(clusterManager)
        clusterManager.markerCollection.setOnInfoWindowAdapter(StopInfoWindowAdapter())
        clusterManager.renderer = CustomClusterRenderer(requireContext(), map, clusterManager)

        createBaseMarkers()

        clusterManager.algorithm = CustomClusteringAlgorithm()
        clusterManager.setOnClusterItemClickListener { item ->
            clickedClusterItem = item
            false
        }
        clusterManager.setOnClusterItemInfoWindowClickListener { stop ->
            findNavController().navigate(MapFragmentDirections.actionMapToStopDetails(stop.type.name, stop.stopId))
        }
    }

    private fun createBaseMarkers() {
        val busDrawable = resources.getDrawable(R.drawable.marker_bus, null) as BitmapDrawable
        val busBitmap = Bitmap.createScaledBitmap(
            busDrawable.bitmap,
            ICON_SIZE,
            ICON_SIZE,
            false
        )
        busMarkerIcon = BitmapDescriptorFactory.fromBitmap(busBitmap)

        val busFavoriteDrawable = resources.getDrawable(R.drawable.marker_bus_favorite, null) as BitmapDrawable
        val busFavoriteBitmap =
            Bitmap.createScaledBitmap(
                busFavoriteDrawable.bitmap,
                ICON_FAV_SIZE,
                ICON_FAV_SIZE,
                false
            )
        busFavoriteMarkerIcon = BitmapDescriptorFactory.fromBitmap(busFavoriteBitmap)


        val tramDrawable = resources.getDrawable(R.drawable.marker_tram, null) as BitmapDrawable
        val tramBitmap = Bitmap.createScaledBitmap(
            tramDrawable.bitmap,
            ICON_SIZE,
            ICON_SIZE,
            false
        )
        tramMarkerIcon = BitmapDescriptorFactory.fromBitmap(tramBitmap)

        val tramFavoriteDrawable = resources.getDrawable(R.drawable.marker_tram_favorite, null) as BitmapDrawable
        val tramFavoriteBitmap =
            Bitmap.createScaledBitmap(
                tramFavoriteDrawable.bitmap,
                ICON_FAV_SIZE,
                ICON_FAV_SIZE,
                false
            )
        tramFavoriteMarkerIcon = BitmapDescriptorFactory.fromBitmap(tramFavoriteBitmap)
    }
}