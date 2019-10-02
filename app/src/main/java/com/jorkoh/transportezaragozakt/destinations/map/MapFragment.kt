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
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
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
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.destinations.FragmentWithToolbar
import com.jorkoh.transportezaragozakt.destinations.toLatLng
import com.jorkoh.transportezaragozakt.repositories.util.Status
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.map_trackings_control.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MapFragment : FragmentWithToolbar() {

    companion object {
        const val ICON_SIZE = 55
        const val ICON_TRACKING_SIZE = 66
        const val ICON_FAV_SIZE = 70

        const val MAX_ZOOM = 17.5f
        const val MIN_ZOOM = 12f
        const val MIN_ZOOM_RURAL = 10f
        var ACTIVE_MIN_ZOOM = MIN_ZOOM
        const val DEFAULT_ZOOM = 16f
        const val MAX_CLUSTERING_ZOOM = 15.5f

        val ZARAGOZA_BOUNDS = LatLngBounds(
            LatLng(41.601261, -0.980633), LatLng(41.766645, -0.792816)
        )
        val RURAL_BOUNDS = LatLngBounds(
            LatLng(41.373278, -1.341434), LatLng(41.839120, -0.511400)
        )
        var ACTIVE_BOUNDS: LatLngBounds = ZARAGOZA_BOUNDS

        val ZARAGOZA_CENTER = LatLng(41.656362, -0.878920)
    }

    private val mapVM: MapViewModel by sharedViewModel(from = { this })
    private val mapSettingsVM: MapSettingsViewModel by sharedViewModel()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<CustomClusterItem>
    private lateinit var clusteringAlgorithm: CustomClusteringAlgorithm<CustomClusterItem>
    private lateinit var map: GoogleMap

    private val busStopsItems = mutableListOf<CustomClusterItem>()
    private val tramStopsItems = mutableListOf<CustomClusterItem>()
    private val ruralTrackingsItems = mutableListOf<CustomClusterItem>()

    private val selectTracking: (RuralTracking) -> Unit = { tracking ->
        trackingsDialog?.dismiss()
        forcedCameraMovementInProgress = true
        mapVM.selectedItemId.postValue(tracking.vehicleId)
        val cameraPosition = CameraPosition.builder()
            .target(tracking.location)
            .zoom(DEFAULT_ZOOM)
            .bearing(0f)
            .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                forcedCameraMovementInProgress = false
            }

            override fun onCancel() {
                forcedCameraMovementInProgress = false
            }

        })
    }
    private var forcedCameraMovementInProgress = false
    private var trackingsDialog: MaterialDialog? = null
    private val trackingsAdapter = TrackingsAdapter(selectTracking)

    @SuppressLint("MissingPermission")
    private val onMyLocationButtonClickListener = GoogleMap.OnMyLocationButtonClickListener {
        runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION) {
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
                displayTrackingsButton = true,
                bottomPaddingDimen = R.dimen.map_destination_map_view_bottom_padding
            )
            childFragmentManager.beginTransaction()
                .add(R.id.map_fragment_container, mapFragment, getString(R.string.map_destination_map_fragment_tag))
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
            map_trackings_button.setOnClickListener {
                if (ruralTrackingsItems.size > 0) {
                    // Display a dialog to select the tracking
                    trackingsDialog = MaterialDialog(requireContext()).show {
                        title(R.string.rural_trackings_available)
                        cancelOnTouchOutside(true)
                        customListAdapter(trackingsAdapter)
                    }
                } else {
                    (requireActivity() as MainActivity).makeSnackbar(getString(R.string.no_trackings_available))
                }
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
        map.setMinZoomPreference(ACTIVE_MIN_ZOOM)
        map.setLatLngBoundsForCameraTarget(ACTIVE_BOUNDS)
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
            mapVM.selectedItemId,
            mapSettingsVM.busFilterEnabled,
            mapSettingsVM.tramFilterEnabled,
            mapSettingsVM.ruralFilterEnabled
        )

        clusterManager.algorithm = clusteringAlgorithm
        clusterManager.setOnClusterItemClickListener { item ->
            mapVM.selectedItemId.postValue(item?.stop?.stopId ?: "")
            false
        }
        map.setOnInfoWindowCloseListener {
            // Don't remove the selectedItemId when animating the camera to a selected rural tracking
            // with an info window already opened
            if (!forcedCameraMovementInProgress) {
                mapVM.selectedItemId.postValue("")
            }
        }
        clusterManager.setOnClusterItemInfoWindowClickListener { item ->
            item.stop?.let { stop ->
                findNavController().navigate(MapFragmentDirections.actionMapToStopDetails(stop.type.name, stop.stopId))
            }
        }
        clusterManager.setAnimation(true)
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
            if (cameraNeedsCentering) {
                onMyLocationButtonClickListener.onMyLocationButtonClick()
            }
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
        mapSettingsVM.busFilterEnabled.observe(mapLifecycleOwner, Observer {
            clusterManager.clusterWithoutCache()
        })
        mapSettingsVM.tramFilterEnabled.observe(mapLifecycleOwner, Observer {
            clusterManager.clusterWithoutCache()
        })
        mapSettingsVM.ruralFilterEnabled.observe(mapLifecycleOwner, Observer { enabled ->
            clusterManager.clusterWithoutCache()
            map_trackings_button.visibility = if (enabled) View.VISIBLE else View.GONE
            ACTIVE_BOUNDS = if (enabled) RURAL_BOUNDS else ZARAGOZA_BOUNDS
            map.setLatLngBoundsForCameraTarget(ACTIVE_BOUNDS)
            ACTIVE_MIN_ZOOM = if (enabled) MIN_ZOOM_RURAL else MIN_ZOOM
            map.setMinZoomPreference(ACTIVE_MIN_ZOOM)
        })

        // Stops
        mapVM.busStopLocations.observe(mapLifecycleOwner, Observer { stops ->
            val items = stops.map { CustomClusterItem(it) }
            clusterManager.removeItems(busStopsItems.minus(items))
            clusterManager.addItems(items.minus(busStopsItems))
            clusterManager.cluster()
            busStopsItems.clear()
            busStopsItems.addAll(items)
        })
        mapVM.tramStopLocations.observe(mapLifecycleOwner, Observer { stops ->
            val items = stops.map { CustomClusterItem(it) }
            clusterManager.removeItems(tramStopsItems.minus(items))
            clusterManager.addItems(items.minus(tramStopsItems))
            clusterManager.cluster()
            tramStopsItems.clear()
            tramStopsItems.addAll(items)
        })
        mapVM.ruralTrackings.observe(mapLifecycleOwner, Observer { trackings ->
            when (trackings.status) {
                Status.SUCCESS -> {
                    trackings.data?.map { CustomClusterItem(it) }?.let { items ->
                        clusterManager.removeItems(ruralTrackingsItems.minus(items))
                        clusterManager.addItems(items.minus(ruralTrackingsItems))
                        clusterManager.cluster()
                        ruralTrackingsItems.clear()
                        ruralTrackingsItems.addAll(items)
                        trackingsAdapter.setNewTrackings(trackings.data)
                    }
                }
                else -> {
                    // Ignore other responses, already filtered on view model
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