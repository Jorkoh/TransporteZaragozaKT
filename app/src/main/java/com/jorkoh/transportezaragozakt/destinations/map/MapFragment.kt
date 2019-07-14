package com.jorkoh.transportezaragozakt.destinations.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.ClusterManager
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.destinations.toLatLng
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.main_container.*
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
            LatLng(41.614155, -0.984636), LatLng(41.774594, -0.7933)
        )
        val ZARAGOZA_CENTER = LatLng(41.656362, -0.878920)
    }

    private val mapVM: MapViewModel by sharedViewModel()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<Stop>
    private lateinit var map: GoogleMap

    private val busStops = mutableListOf<Stop>()
    private val tramStops = mutableListOf<Stop>()

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

        var mapFragment =
            childFragmentManager.findFragmentByTag(getString(R.string.map_destination_map_fragment_tag)) as CustomSupportMapFragment?
        if (mapFragment == null) {
            mapFragment = CustomSupportMapFragment.newInstance(true)
            childFragmentManager.beginTransaction()
                .add(R.id.map_fragment_container, mapFragment, getString(R.string.map_destination_map_fragment_tag))
                .commit()
            childFragmentManager.executePendingTransactions()
        }
        mapFragment.getMapAsync { map ->
            this.map = map
            setupMap(!ZARAGOZA_BOUNDS.contains(map.cameraPosition.target))
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

        map.clear()
        busStops.clear()
        tramStops.clear()

        styleMap()
        setupClusterManager()
        setupObservers()

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
        clusterManager.markerCollection.setOnInfoWindowAdapter(StopInfoWindowAdapter(requireContext()))
        clusterManager.renderer = CustomClusterRenderer(requireContext(), map, clusterManager)

        clusterManager.algorithm = CustomClusteringAlgorithm()
        clusterManager.setOnClusterItemInfoWindowClickListener { stop ->
            findNavController().navigate(MapFragmentDirections.actionMapToStopDetails(stop.type.name, stop.stopId))
        }
    }

    private fun setupObservers() {
        // Map options
        mapVM.mapType.observe(viewLifecycleOwner, Observer { mapType ->
            map.mapType = mapType
        })
        mapVM.trafficEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            map.isTrafficEnabled = enabled
        })

        // Map style
        mapVM.isDarkMap.observe(viewLifecycleOwner, Observer { isDarkMap ->
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    if (isDarkMap) R.raw.map_style_dark else R.raw.map_style
                )
            )
        })

        // Stop type filters
        mapVM.busFilterEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            if (enabled) {
                clusterManager.addItems(busStops)
            } else {
                busStops.forEach { clusterManager.removeItem(it) }
            }
            clusterManager.cluster()
        })
        mapVM.tramFilterEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            if (enabled) {
                clusterManager.addItems(tramStops)
            } else {
                tramStops.forEach { clusterManager.removeItem(it) }
            }
            clusterManager.cluster()
        })

        // Stops
        mapVM.busStopLocations.observe(viewLifecycleOwner, Observer { stops ->
            if (mapVM.busFilterEnabled.value == true) {
                stops.forEach { clusterManager.removeItem(it) }
                clusterManager.addItems(stops)
                clusterManager.cluster()
            }
            busStops.clear()
            busStops.addAll(stops)
        })
        mapVM.tramStopLocations.observe(viewLifecycleOwner, Observer { stops ->
            if (mapVM.tramFilterEnabled.value == true) {
                stops.forEach { clusterManager.removeItem(it) }
                clusterManager.addItems(stops)
                clusterManager.cluster()
            }
            tramStops.clear()
            tramStops.addAll(stops)
        })
    }
}