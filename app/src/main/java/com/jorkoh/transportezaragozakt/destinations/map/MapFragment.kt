package com.jorkoh.transportezaragozakt.destinations.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
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
import com.jorkoh.transportezaragozakt.destinations.FragmentWithToolbar
import com.jorkoh.transportezaragozakt.destinations.toLatLng
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MapFragment : FragmentWithToolbar() {

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

    private val mapVM: MapViewModel by sharedViewModel(from = { this })
    private val mapSettingsVM: MapSettingsViewModel by sharedViewModel()

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.map_destination, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        var mapFragment =
            childFragmentManager.findFragmentByTag(getString(R.string.map_destination_map_fragment_tag)) as CustomSupportMapFragment?
        if (mapFragment == null) {
            mapFragment = CustomSupportMapFragment.newInstance(
                displayFilters = true,
                bottomMargin = 56
            )
            childFragmentManager.beginTransaction()
                .add(R.id.map_fragment_container, mapFragment, getString(R.string.map_destination_map_fragment_tag))
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
        busStops.clear()
        tramStops.clear()

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

    private fun configureMap() {
        clusterManager = ClusterManager(context, map)

        map.setOnMarkerClickListener(clusterManager)
        map.setInfoWindowAdapter(clusterManager.markerManager)
        map.setOnInfoWindowClickListener(clusterManager)
        map.setOnCameraIdleListener(clusterManager)
        clusterManager.markerCollection.setOnInfoWindowAdapter(StopInfoWindowAdapter(requireContext()))
        clusterManager.renderer = CustomClusterRenderer(requireContext(), map, clusterManager, mapVM.selectedStopId)
        clusterManager.algorithm = CustomClusteringAlgorithm()
        clusterManager.setOnClusterItemClickListener { stop ->
            mapVM.selectedStopId.postValue(stop.stopId)
            false
        }
        map.setOnInfoWindowCloseListener {
            mapVM.selectedStopId.postValue("")
        }

        clusterManager.setOnClusterItemInfoWindowClickListener { stop ->
            findNavController().navigate(MapFragmentDirections.actionMapToStopDetails(stop.type.name, stop.stopId))
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

        // Stop type filters
        mapSettingsVM.busFilterEnabled.observe(mapLifecycleOwner, Observer { enabled ->
            if (enabled) {
                clusterManager.addItems(busStops)
            } else {
                busStops.forEach { clusterManager.removeItem(it) }
            }
            clusterManager.cluster()
        })
        mapSettingsVM.tramFilterEnabled.observe(mapLifecycleOwner, Observer { enabled ->
            if (enabled) {
                clusterManager.addItems(tramStops)
            } else {
                tramStops.forEach { clusterManager.removeItem(it) }
            }
            clusterManager.cluster()
        })

        // Stops
        mapVM.busStopLocations.observe(mapLifecycleOwner, Observer { stops ->
            if (mapSettingsVM.busFilterEnabled.value == true) {
                stops.forEach { clusterManager.removeItem(it) }
                clusterManager.addItems(stops)
                clusterManager.cluster()
            }
            busStops.clear()
            busStops.addAll(stops)
        })
        mapVM.tramStopLocations.observe(mapLifecycleOwner, Observer { stops ->
            if (mapSettingsVM.tramFilterEnabled.value == true) {
                stops.forEach { clusterManager.removeItem(it) }
                clusterManager.addItems(stops)
                clusterManager.cluster()
            }
            tramStops.clear()
            tramStops.addAll(stops)
        })
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