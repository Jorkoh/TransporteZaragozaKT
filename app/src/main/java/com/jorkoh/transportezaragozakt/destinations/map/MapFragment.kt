package com.jorkoh.transportezaragozakt.destinations.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
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
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.destinations.FragmentWithToolbar
import com.jorkoh.transportezaragozakt.destinations.toLatLng
import com.jorkoh.transportezaragozakt.repositories.util.Status
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MapFragment : FragmentWithToolbar() {

    companion object {
        const val ICON_SIZE = 55
        const val ICON_TRACKING_SIZE = 66
        const val ICON_FAV_SIZE = 70
        const val MAX_ZOOM = 17.5f
        const val MIN_ZOOM = 12f
        const val DEFAULT_ZOOM = 16f
        const val MAX_CLUSTERING_ZOOM = 15.5f
        val ZARAGOZA_BOUNDS = LatLngBounds(
            LatLng(41.601261, -0.980633), LatLng(41.766645, -0.792816)
        )
        val ZARAGOZA_CENTER = LatLng(41.656362, -0.878920)
    }

    private val mapVM: MapViewModel by sharedViewModel(from = { this })
    private val mapSettingsVM: MapSettingsViewModel by sharedViewModel()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<CustomClusterItem>
    private lateinit var clusteringAlgorithm: CustomClusteringAlgorithm<CustomClusterItem>
    private lateinit var algorithmDecorator: PreCachingAlgorithmDecorator<CustomClusterItem>
    private lateinit var map: GoogleMap

    private val busStopsItems = mutableListOf<CustomClusterItem>()
    private val tramStopsItems = mutableListOf<CustomClusterItem>()
    private val ruralTrackingsItems = mutableListOf<CustomClusterItem>()

    private val filterChanged: Observer<Boolean> = Observer {
        algorithmDecorator.clearCache()
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
            Log.d("TESTING STUFF3", "New map fragment")
            mapFragment = CustomSupportMapFragment.newInstance(
                displayFilters = true,
                bottomPaddingDimen = R.dimen.map_destination_map_view_bottom_padding
            )
            childFragmentManager.beginTransaction()
                .add(R.id.map_fragment_container, mapFragment, getString(R.string.map_destination_map_fragment_tag))
                .commit()
        } else {
            Log.d("TESTING STUFF3", "Old map fragment")
        }
        mapFragment.getMapAsync { newMap ->
            val mapNeedsSetup = !::map.isInitialized
            val cameraNeedsCentering = !ZARAGOZA_BOUNDS.contains(newMap.cameraPosition.target)
            this.map = newMap

            if (mapNeedsSetup) {
                setupMap(cameraNeedsCentering)
            }
            /**/
            with(mapFragment.viewLifecycleOwner) {
                if (lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                    setupObservers(this)
                }
            }

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
                if (cameraNeedsCentering) {
                    onMyLocationButtonClickListener.onMyLocationButtonClick()
                }
            }
            /**/
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
        map.setMinZoomPreference(MIN_ZOOM)
        map.setLatLngBoundsForCameraTarget(ZARAGOZA_BOUNDS)
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
    }

    private fun configureMap() {
        clusterManager = ClusterManager(context, map)

        clusteringAlgorithm = CustomClusteringAlgorithm(
            mapSettingsVM.busFilterEnabled,
            mapSettingsVM.tramFilterEnabled,
            mapSettingsVM.ruralFilterEnabled
        )

        map.setOnMarkerClickListener(clusterManager)
        map.setInfoWindowAdapter(clusterManager.markerManager)
        map.setOnInfoWindowClickListener(clusterManager)
        map.setOnCameraIdleListener(clusterManager)
        clusterManager.markerCollection.setOnInfoWindowAdapter(CustomInfoWindowAdapter(requireContext()))

        clusterManager.renderer = CustomClusterRenderer(
            requireContext(),
            map,
            clusterManager,
            mapVM.selectedStopId,
            mapSettingsVM.busFilterEnabled,
            mapSettingsVM.tramFilterEnabled,
            mapSettingsVM.ruralFilterEnabled
        )

        algorithmDecorator = PreCachingAlgorithmDecorator(clusteringAlgorithm)
        clusterManager.algorithm = algorithmDecorator
        clusterManager.setOnClusterItemClickListener { item ->
            mapVM.selectedStopId.postValue(item?.stop?.stopId ?: "")
            false
        }
        map.setOnInfoWindowCloseListener {
            mapVM.selectedStopId.postValue("")
        }
        clusterManager.setOnClusterItemInfoWindowClickListener { item ->
            item.stop?.let { stop ->
                findNavController().navigate(MapFragmentDirections.actionMapToStopDetails(stop.type.name, stop.stopId))
            }
        }
        clusterManager.setAnimation(true)
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
        mapSettingsVM.busFilterEnabled.observe(mapLifecycleOwner, filterChanged)
        mapSettingsVM.tramFilterEnabled.observe(mapLifecycleOwner, filterChanged)
        mapSettingsVM.ruralFilterEnabled.observe(mapLifecycleOwner, filterChanged)

        // Stops
        mapVM.busStopLocations.observe(mapLifecycleOwner, Observer { stops ->
            Log.d("TESTING STUFF2", "Observed ${stops.size} bus stops")
            val items = stops.map { CustomClusterItem(it) }
            clusteringAlgorithm.removeItems(busStopsItems.minus(items))
            clusterManager.addItems(items.minus(busStopsItems))
            clusterManager.cluster()
            busStopsItems.clear()
            busStopsItems.addAll(items)
        })
        mapVM.tramStopLocations.observe(mapLifecycleOwner, Observer { stops ->
            val items = stops.map { CustomClusterItem(it) }
            clusteringAlgorithm.removeItems(tramStopsItems.minus(items))
            clusterManager.addItems(items.minus(tramStopsItems))
            clusterManager.cluster()
            tramStopsItems.clear()
            tramStopsItems.addAll(items)
        })
        mapVM.ruralTrackings.observe(mapLifecycleOwner, Observer { trackings ->
            when (trackings.status) {
                Status.SUCCESS -> {
                    trackings.data?.map { CustomClusterItem(it) }?.let { items ->
                        clusteringAlgorithm.removeItems(ruralTrackingsItems.minus(items))
                        clusterManager.addItems(items.minus(ruralTrackingsItems))
                        clusterManager.cluster()
                        ruralTrackingsItems.clear()
                        ruralTrackingsItems.addAll(items)
                    }
                }
                Status.ERROR -> {
                    (requireActivity() as MainActivity).makeSnackbar("ERROR LOADING TRACKINGS")
                }
                Status.LOADING -> {
                    (requireActivity() as MainActivity).makeSnackbar("LOADING TRACKINGS")
                }
            }
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