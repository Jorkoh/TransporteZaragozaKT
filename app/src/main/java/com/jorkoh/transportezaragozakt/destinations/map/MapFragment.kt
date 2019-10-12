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
import java.text.DateFormat


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
            LatLng(41.373278, -1.341434), LatLng(41.954787, -0.520798)
        )
        var ACTIVE_BOUNDS: LatLngBounds = RURAL_BOUNDS

        val ZARAGOZA_CENTER = LatLng(41.656362, -0.878920)
    }

    private val mapVM: MapViewModel by sharedViewModel(from = { this })
    private val mapSettingsVM: MapSettingsViewModel by sharedViewModel()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<CustomClusterItem>
    private lateinit var clusteringAlgorithm: CustomClusteringAlgorithm<CustomClusterItem>
    private lateinit var clusterRenderer: CustomClusterRenderer
    private lateinit var map: GoogleMap

    private val busStopsItems = mutableListOf<CustomClusterItem>()
    private val tramStopsItems = mutableListOf<CustomClusterItem>()
    private val ruralStopsItems = mutableListOf<CustomClusterItem>()
    private val ruralTrackingsItems = mutableListOf<CustomClusterItem>()

    private val selectTracking: (RuralTracking) -> Unit = { tracking ->
        trackingsDialog?.dismiss()
        val cameraPosition = CameraPosition.builder()
            .target(tracking.location)
            .zoom(DEFAULT_ZOOM)
            .bearing(0f)
            .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                mapVM.selectedItemId.postValue(tracking.vehicleId)
                clusterRenderer.getMarker(CustomClusterItem(tracking))?.showInfoWindow()
            }

            override fun onCancel() {
            }

        })
    }
    private var trackingsDialog: MaterialDialog? = null
    private val trackingsAdapter = TrackingsAdapter(selectTracking)

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
                updateTrackingsDialogDistance()
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
                        title(text = getTrackingsTitle())
                        cancelOnTouchOutside(true)
                        customListAdapter(trackingsAdapter)
                    }
                    updateTrackingsDialogDistance()
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

        clusterRenderer = CustomClusterRenderer(
            requireContext(),
            map,
            clusterManager,
            mapVM.selectedItemId,
            mapSettingsVM.busFilterEnabled,
            mapSettingsVM.tramFilterEnabled,
            mapSettingsVM.ruralFilterEnabled
        )
        clusterManager.renderer = clusterRenderer

        clusterManager.algorithm = clusteringAlgorithm
        clusterManager.setOnClusterItemClickListener { item ->
            mapVM.selectedItemId.postValue(item?.stop?.stopId ?: item?.ruralTracking?.vehicleId ?: "")
            false
        }
        map.setOnInfoWindowCloseListener {
            mapVM.selectedItemId.postValue("")
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
            clusterManager.clusterWithoutCacheOrAnimation()
        })
        mapSettingsVM.tramFilterEnabled.observe(mapLifecycleOwner, Observer {
            clusterManager.clusterWithoutCacheOrAnimation()
        })
        mapSettingsVM.ruralFilterEnabled.observe(mapLifecycleOwner, Observer { enabled ->
            clusterManager.clusterWithoutCacheOrAnimation()
            map_trackings_button.visibility = if (enabled) View.VISIBLE else View.GONE
            ACTIVE_BOUNDS = if (enabled) RURAL_BOUNDS else ZARAGOZA_BOUNDS
            map.setLatLngBoundsForCameraTarget(ACTIVE_BOUNDS)
            ACTIVE_MIN_ZOOM = if (enabled) MIN_ZOOM_RURAL else MIN_ZOOM
            if (!enabled && map.cameraPosition.zoom < ACTIVE_MIN_ZOOM) {
                // If the map was zoomed out too far zoom in gently
                map.animateCamera(CameraUpdateFactory.zoomTo(ACTIVE_MIN_ZOOM), object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        map.setMinZoomPreference(ACTIVE_MIN_ZOOM)
                    }

                    override fun onCancel() {
                        map.setMinZoomPreference(ACTIVE_MIN_ZOOM)
                    }
                })
            } else {
                map.setMinZoomPreference(ACTIVE_MIN_ZOOM)
            }
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
        mapVM.ruralStopLocations.observe(mapLifecycleOwner, Observer { stops ->
            val items = stops.map { CustomClusterItem(it) }
            clusterManager.removeItems(ruralStopsItems.minus(items))
            clusterManager.addItems(items.minus(ruralStopsItems))
            clusterManager.cluster()
            ruralStopsItems.clear()
            ruralStopsItems.addAll(items)
        })
        // Trackings
        mapVM.ruralTrackings.observe(mapLifecycleOwner, Observer { trackings ->
            if (trackings.status == Status.SUCCESS && trackings.data != null) {
                val items = trackings.data.map { CustomClusterItem(it) }
                clusterManager.removeItems(ruralTrackingsItems.minus(items))
                clusterManager.addItems(items.minus(ruralTrackingsItems))
                clusterManager.cluster()
                ruralTrackingsItems.clear()
                ruralTrackingsItems.addAll(items)
                // Update the trackings selector
                trackingsAdapter.setNewTrackings(trackings.data)
                trackingsDialog?.title(text = getTrackingsTitle())
                updateTrackingsDialogDistance()
            }
        })
    }

    private fun getTrackingsTitle(): String {
        val time = ruralTrackingsItems[0].ruralTracking?.updatedAt
        return if (time != null) {
            getString(
                R.string.rural_trackings_template,
                DateFormat.getTimeInstance(DateFormat.SHORT).format(ruralTrackingsItems[0].ruralTracking?.updatedAt)
            )
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
    }
}