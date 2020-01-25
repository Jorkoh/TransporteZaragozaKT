package com.jorkoh.transportezaragozakt.destinations.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.Fade
import androidx.transition.Slide
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
import com.google.maps.android.SphericalUtil
import com.google.maps.android.clustering.ClusterManager
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragment
import com.jorkoh.transportezaragozakt.destinations.utils.*
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.map_info_window_transition.*
import kotlinx.android.synthetic.main.map_trackings_control.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.text.DateFormat
import java.util.concurrent.TimeUnit

class MapFragment : FragmentWithToolbar() {

    companion object {
        const val ICON_SIZE = 55
        const val ICON_TRACKING_SIZE = 66
        const val ICON_FAV_SIZE = 70

        const val MAX_ZOOM = 17.5f
        const val MIN_ZOOM = 12f
        const val MIN_ZOOM_RURAL = 10f
        const val DEFAULT_ZOOM = 16f
        const val MAX_CLUSTERING_ZOOM = 15.5f

        val ZARAGOZA_BOUNDS = LatLngBounds(
            LatLng(41.601261, -0.980633), LatLng(41.766645, -0.792816)
        )
        val RURAL_BOUNDS = LatLngBounds(
            LatLng(41.373278, -1.341434), LatLng(41.954787, -0.520798)
        )

        val ZARAGOZA_CENTER = LatLng(41.656362, -0.878920)
    }

    private var activeMinZoom = MIN_ZOOM
    private var activeBounds: LatLngBounds = RURAL_BOUNDS

    private val mapVM: MapViewModel by sharedViewModel(from = { this })
    private val mapSettingsVM: MapSettingsViewModel by sharedViewModel()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<CustomClusterItem>
    private lateinit var infoWindowAdapter: CustomInfoWindowAdapter
    private lateinit var clusteringAlgorithm: CustomClusteringAlgorithm<CustomClusterItem>
    private lateinit var clusterRenderer: CustomClusterRenderer
    private lateinit var map: GoogleMap
    private lateinit var mapFragment: CustomSupportMapFragment

    private val busStopsItems = mutableListOf<CustomClusterItem>()
    private val tramStopsItems = mutableListOf<CustomClusterItem>()
    private val ruralStopsItems = mutableListOf<CustomClusterItem>()
    private val ruralTrackingsItems = mutableListOf<CustomClusterItem>()

    private val selectTracking: (RuralTracking) -> Unit = { tracking ->
        trackingsDialog?.dismiss()
        lifecycleScope.launchWhenStarted {
            mapVM.selectedItemId.send(tracking.vehicleId)
        }
    }
    private var trackingsDialog: MaterialDialog? = null
    private val trackingsAdapter = TrackingsAdapter(selectTracking)

    private val onMyLocationButtonClickListener = GoogleMap.OnMyLocationButtonClickListener {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                if (activeBounds.contains(location.toLatLng())) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is the transition to be used for non-shared elements when we are opening the detail screen.
        exitTransition = transitionTogether {
            this += Slide(Gravity.TOP).apply {
                duration = ANIMATE_INTO_DETAILS_SCREEN_DURATION / 2
                interpolator = FAST_OUT_LINEAR_IN
                mode = Slide.MODE_OUT
                addTarget(R.id.map_appBar)
            }
            this += Fade().apply {
                duration = 1
                startDelay = ANIMATE_INTO_DETAILS_SCREEN_DURATION -1
                mode = Fade.MODE_OUT
                addTarget(R.id.map_fake_transition_background_image)
            }
        }

        // This is the transition to be used for non-shared elements when we are return back from the detail screen.
        reenterTransition = Slide(Gravity.TOP).apply {
            duration = ANIMATE_OUT_OF_DETAILS_SCREEN_DURATION / 2
            interpolator = LINEAR_OUT_SLOW_IN
            mode = Slide.MODE_IN
            addTarget(R.id.map_appBar)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        postponeEnterTransition(300L, TimeUnit.MILLISECONDS)
        return inflater.inflate(R.layout.map_destination, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        var tempMapFragment =
            childFragmentManager.findFragmentByTag(getString(R.string.map_destination_map_fragment_tag)) as CustomSupportMapFragment?
        if (tempMapFragment == null) {
            tempMapFragment = CustomSupportMapFragment.newInstance(
                displayFilters = true,
                displayTrackingsButton = true,
                bottomPaddingDimen = R.dimen.map_destination_map_view_bottom_padding
            )
            childFragmentManager.beginTransaction()
                .add(R.id.map_fragment_container, tempMapFragment, getString(R.string.map_destination_map_fragment_tag))
                .commit()
        }
        mapFragment = tempMapFragment
        mapFragment.getMapAsync { newMap ->
            val mapNeedsSetup = !::map.isInitialized
            val cameraNeedsCentering = !activeBounds.contains(newMap.cameraPosition.target)
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

            map_trackings_button.setOnClickListener(DebounceClickListener {
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
            })

            startPostponedEnterTransition()
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
        map.setMinZoomPreference(activeMinZoom)
        map.setLatLngBoundsForCameraTarget(activeBounds)
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
    }

    private fun configureMap() {
        clusterManager = ClusterManager(context, map)
        infoWindowAdapter = CustomInfoWindowAdapter(requireContext())

        clusteringAlgorithm = CustomClusteringAlgorithm(
            mapSettingsVM.busFilterEnabled,
            mapSettingsVM.tramFilterEnabled,
            mapSettingsVM.ruralFilterEnabled
        )

        map.setOnMarkerClickListener(clusterManager)
        map.setInfoWindowAdapter(clusterManager.markerManager)
        map.setOnInfoWindowClickListener(clusterManager)
        map.setOnCameraIdleListener(clusterManager)
        clusterManager.markerCollection.setOnInfoWindowAdapter(infoWindowAdapter)

        clusterRenderer = CustomClusterRenderer(
            requireContext(),
            map,
            clusterManager,
            mapVM.preservedItemId,
            mapSettingsVM.busFilterEnabled,
            mapSettingsVM.tramFilterEnabled,
            mapSettingsVM.ruralFilterEnabled
        )
        clusterManager.renderer = clusterRenderer

        clusterManager.algorithm = clusteringAlgorithm
        clusterManager.setOnClusterItemClickListener { item ->
            mapVM.preservedItemId.postValue(item.itemId)
            false
        }
        map.setOnInfoWindowCloseListener {
            mapVM.preservedItemId.postValue("")
        }
        clusterManager.setOnClusterItemInfoWindowClickListener { item ->
            item.stop?.let { stop ->
                // Since the InfoWindow is not a live view, inflate and position a fake InfoWindow to use in the transition
                val screenPosition = map.projection.toScreenLocation(stop.location)
                val fakeTransitionInfoWindow = infoWindowAdapter.inflateFakeTransitionInfoWindow(stop)
                // Need a snapshot of the map because the surface view blanks when the transition starts
                map.snapshot { mapSnapshot ->
                    mapFragment.addFakeTransitionViews(FakeTransitionInfoWindow(fakeTransitionInfoWindow, screenPosition, stop.isFavorite), mapSnapshot)
                    findNavController().navigate(
                        MapFragmentDirections.actionMapToStopDetails(stop.type.name, stop.stopId),
                        FragmentNavigatorExtras(
                            map_info_window_transition_card to StopDetailsFragment.TRANSITION_NAME_BACKGROUND,
                            map_info_window_transition_mirror_body to StopDetailsFragment.TRANSITION_NAME_BODY,
                            map_info_window_transition_layout to StopDetailsFragment.TRANSITION_NAME_APPBAR,
                            map_info_window_transition_mirror_toolbar to StopDetailsFragment.TRANSITION_NAME_TOOLBAR,
                            map_info_window_transition_type_image to StopDetailsFragment.TRANSITION_NAME_IMAGE,
                            map_info_window_transition_title to StopDetailsFragment.TRANSITION_NAME_TITLE,
                            map_info_window_transition_lines_layout to StopDetailsFragment.TRANSITION_NAME_LINES,
                            map_info_window_transition_mirror_fab to StopDetailsFragment.TRANSITION_NAME_FAB,

                            map_info_window_transition_number to StopDetailsFragment.TRANSITION_NAME_FIRST_ELEMENT_SECOND_ROW
                        )
                    )
                }
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
        mapSettingsVM.mapAnimationsEnabled.observe(mapLifecycleOwner, Observer { mapAnimationsEnabled ->
            clusterManager.setAnimation(mapAnimationsEnabled)
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
            activeBounds = if (enabled) RURAL_BOUNDS else ZARAGOZA_BOUNDS
            map.setLatLngBoundsForCameraTarget(activeBounds)
            activeMinZoom = if (enabled) MIN_ZOOM_RURAL else MIN_ZOOM
            if (!enabled && map.cameraPosition.zoom < activeMinZoom) {
                // If the map was zoomed out too far zoom in gently
                map.animateCamera(CameraUpdateFactory.zoomTo(activeMinZoom), object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        map.setMinZoomPreference(activeMinZoom)
                    }

                    override fun onCancel() {
                        map.setMinZoomPreference(activeMinZoom)
                    }
                })
            } else {
                map.setMinZoomPreference(activeMinZoom)
            }
        })

        // Stops
        mapVM.busStopLocations.observe(mapLifecycleOwner, Observer { stops ->
            renewStopItems(stops.map { CustomClusterItem(it) }, busStopsItems)
        })
        mapVM.tramStopLocations.observe(mapLifecycleOwner, Observer { stops ->
            renewStopItems(stops.map { CustomClusterItem(it) }, tramStopsItems)
        })
        mapVM.ruralStopLocations.observe(mapLifecycleOwner, Observer { stops ->
            renewStopItems(stops.map { CustomClusterItem(it) }, ruralStopsItems)
        })

        // Trackings
        mapVM.ruralTrackings.observe(mapLifecycleOwner, Observer { trackings ->
            trackings?.let {
                renewTrackingItems(trackings, ruralTrackingsItems)
                // Update the trackings selector
                trackingsAdapter.setNewTrackings(trackings)
                trackingsDialog?.title(text = getTrackingsTitle())
                updateTrackingsDialogDistance()
            }
        })

        lifecycleScope.launchWhenStarted {
            for (selectedItemId in mapVM.selectedItemId) {
                findItem(selectedItemId)?.let { selectedItem ->
                    val cameraPosition = CameraPosition.builder()
                        .target(selectedItem.position)
                        .zoom(DEFAULT_ZOOM)
                        .bearing(0f)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), object : GoogleMap.CancelableCallback {
                        override fun onFinish() {
                            clusterRenderer.getMarker(selectedItem)?.showInfoWindow()
                            mapVM.preservedItemId.postValue(selectedItemId)
                        }

                        override fun onCancel() {}
                    })
                }
            }
        }
    }

    private fun renewStopItems(newItems: List<CustomClusterItem>, oldItems: MutableList<CustomClusterItem>) {
        val preservedItemId = mapVM.preservedItemId.value

        clusterManager.removeItems(oldItems.minus(newItems))
        clusterManager.addItems(newItems.minus(oldItems))
        clusterManager.cluster()
        oldItems.clear()
        oldItems.addAll(newItems)

        if (preservedItemId != null && newItems.any { it.itemId == preservedItemId }) {
            clusterRenderer.getMarker(newItems.find { item ->
                item.itemId == preservedItemId
            })?.showInfoWindow()
            mapVM.preservedItemId.postValue(preservedItemId)
        }
    }

    private fun renewTrackingItems(newTrackings: List<RuralTracking>, oldItems: MutableList<CustomClusterItem>) {
        val preservedItemId = mapVM.preservedItemId.value
        val oldMarker = clusterRenderer.getMarker(oldItems.find { item ->
            item.itemId == preservedItemId
        })

        val newItems = newTrackings.map { CustomClusterItem(it) }
        clusterManager.removeItems(oldItems.minus(newItems))
        clusterManager.addItems(newItems.minus(oldItems))
        clusterManager.cluster()
        oldItems.clear()
        oldItems.addAll(newItems)

        val newTracking = newTrackings.firstOrNull { it.vehicleId == preservedItemId }
        if (preservedItemId != null && newTracking != null) {
            // If the selected item is a tracking that exists after the renewal, it has changed position
            // and the previous one was on the screen let's recenter to it
            if (oldMarker != null
                && SphericalUtil.computeDistanceBetween(oldMarker.position, newTracking.location) > 1
                && map.projection.visibleRegion.latLngBounds.contains(oldMarker.position)
            ) {
                lifecycleScope.launchWhenStarted {
                    mapVM.selectedItemId.send(preservedItemId)
                }
            } else {
                clusterRenderer.getMarker(newItems.find { item ->
                    item.itemId == preservedItemId
                })?.showInfoWindow()
                mapVM.preservedItemId.postValue(preservedItemId)
            }
        }
    }

    private fun findItem(itemId: String?): CustomClusterItem? =
        busStopsItems.plus(tramStopsItems).plus(ruralStopsItems).plus(ruralTrackingsItems).find { item ->
            item.itemId == itemId
        }

    private fun getTrackingsTitle(): String {
        val time = ruralTrackingsItems.getOrNull(0)?.ruralTracking?.updatedAt
        return if (time != null) {
            getString(R.string.rural_trackings_template, DateFormat.getTimeInstance(DateFormat.SHORT).format(time))
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