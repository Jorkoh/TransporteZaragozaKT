package com.jorkoh.transportezaragozakt.destinations.map

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.db.TagInfo
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragment
import com.jorkoh.transportezaragozakt.repositories.Resource
import com.jorkoh.transportezaragozakt.repositories.Status
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        const val ICON_SIZE = 55
        const val ICON_FAV_SIZE = 70
        const val MAX_ZOOM = 17.5f
        const val MIN_ZOOM = 12f
        const val DEFAULT_ZOOM = 15.5f
        val ZARAGOZA_BOUNDS = LatLngBounds(
            LatLng(41.6000, -1.08125), LatLng(41.774594, -0.7933)
        )
        val ZARAGOZA_CENTER = LatLng(41.656362, -0.878920)
    }

    private val mapVM: MapViewModel by sharedViewModel()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap

    private lateinit var busMarker: MarkerOptions
    private lateinit var busFavoriteMarker: MarkerOptions
    private lateinit var tramMarker: MarkerOptions
    private lateinit var tramFavoriteMarker: MarkerOptions

    private val mapBusStopsMarkers = mutableMapOf<String, Marker>()
    private val mapTramStopsMarkers = mutableMapOf<String, Marker>()

    private val stopLocationsObserver: (Resource<List<Stop>?>) -> Unit = { stopsResource ->
        if (stopsResource.status == Status.SUCCESS) {
            stopsResource.data?.let { stops ->
                stops.forEach { stop ->
                    val baseMarker = when (stop.type) {
                        StopType.BUS -> if (stop.isFavorite) busFavoriteMarker else busMarker
                        StopType.TRAM -> if (stop.isFavorite) tramFavoriteMarker else tramMarker
                    }
                    val markerCollection = when (stop.type) {
                        StopType.BUS -> mapBusStopsMarkers
                        StopType.TRAM -> mapTramStopsMarkers
                    }

                    val newMarker = map.addMarker(baseMarker.title(stop.title).position(stop.location))
                    newMarker.tag = TagInfo(stop.id, stop.type)
                    if (markerCollection[stop.id]?.isInfoWindowShown == true) {
                        newMarker.showInfoWindow()
                    }
                    markerCollection[stop.id]?.remove()
                    markerCollection[stop.id] = newMarker
                }
            }
        }
    }

    private val mapTypeObserver: (Int) -> Unit = { mapType ->
        map.mapType = mapType
    }

    private val trafficEnabledObserver: (Boolean) -> Unit = { enabled ->
        map.isTrafficEnabled = enabled
    }

    private val busFilterEnabledObserver: (Boolean) -> Unit = { visibility ->
        mapBusStopsMarkers.forEach { mapBusStopsMarker ->
            mapBusStopsMarker.value.isVisible = visibility
        }
    }

    private val tramFilterEnabledObserver: (Boolean) -> Unit = { visibility ->
        mapTramStopsMarkers.forEach { mapTramStopsMarker ->
            mapTramStopsMarker.value.isVisible = visibility
        }
    }

    private val onInfoWindowClickListener = GoogleMap.OnInfoWindowClickListener { marker ->
        marker?.let {
            val markerInfo = marker.tag
            if (markerInfo is TagInfo) {
                val bundle = Bundle().apply {
                    putString(StopDetailsFragment.STOP_ID_KEY, markerInfo.id)
                    putString(StopDetailsFragment.STOP_TYPE_KEY, markerInfo.type.name)
                }
                findNavController().navigate(R.id.action_map_to_stopDetails, bundle)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val onMyLocationButtonClickListener = GoogleMap.OnMyLocationButtonClickListener {
        runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null && ZARAGOZA_BOUNDS.contains(location.toLatLng())) {
                    val cameraPosition = CameraPosition.builder()
                        .target(location.toLatLng())
                        .zoom(DEFAULT_ZOOM)
                        .bearing(0f)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        }
        true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapVM.init()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get and (if needed) initialize the map fragment programmatically
        var mapFragment = childFragmentManager.findFragmentByTag("mapFragment") as CustomSupportMapFragment?
        if (mapFragment == null) {
            //TODO: REMOVE THIS TESTING THING
            Log.d("TESTING STUFF", "Creating new map")
            Log.d("TESTING STUFF", "Styled: ${mapVM.mapHasBeenStyled}")

            mapFragment = CustomSupportMapFragment()
            childFragmentManager.beginTransaction()
                .add(R.id.map_fragment_container, mapFragment, "mapFragment")
                .commit()
            childFragmentManager.executePendingTransactions()
        } else {
            //TODO: REMOVE THIS TESTING THING
            Log.d("TESTING STUFF", "Map already exists")
            Log.d("TESTING STUFF", "Styled: ${mapVM.mapHasBeenStyled}")
        }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = checkNotNull(googleMap)

        styleMap()
        map.setOnInfoWindowClickListener(onInfoWindowClickListener)
        mapVM.getBusStopLocations().observe(this, Observer(stopLocationsObserver))
        mapVM.getTramStopLocations().observe(this, Observer(stopLocationsObserver))
        mapVM.getMapType().observe(this, Observer(mapTypeObserver))
        mapVM.getTrafficEnabled().observe(this, Observer(trafficEnabledObserver))
        mapVM.getBusFilterEnabled().observe(this, Observer(busFilterEnabledObserver))
        mapVM.getTramFilterEnabled().observe(this, Observer(tramFilterEnabledObserver))
    }

    private fun styleMap() {
        createBaseMarkers()

        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
        map.setMaxZoomPreference(MAX_ZOOM)
        map.setMinZoomPreference(MIN_ZOOM)
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.setLatLngBoundsForCameraTarget(ZARAGOZA_BOUNDS)
        //Don't center the camera when coming back from orientation change
        if (!mapVM.mapHasBeenStyled) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    ZARAGOZA_CENTER,
                    DEFAULT_ZOOM
                )
            )
        }

        runWithPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            options = QuickPermissionsOptions(
                handleRationale = true,
                rationaleMessage = getString(R.string.location_rationale),
                handlePermanentlyDenied = false,
                permissionsDeniedMethod = {
                    mapVM.mapHasBeenStyled = true
                },
                permanentDeniedMethod = {
                    mapVM.mapHasBeenStyled = true
                })
        ) {
            @SuppressLint("MissingPermission")
            map.isMyLocationEnabled = true
            map.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener)
            if (!mapVM.mapHasBeenStyled) {
                onMyLocationButtonClickListener.onMyLocationButtonClick()
            }
            mapVM.mapHasBeenStyled = true
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
        busMarker = MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(busBitmap))
            .anchor(0.5f, 0.5f)

        val busFavoriteDrawable = resources.getDrawable(R.drawable.marker_bus_favorite, null) as BitmapDrawable
        val busFavoriteBitmap =
            Bitmap.createScaledBitmap(
                busFavoriteDrawable.bitmap,
                ICON_FAV_SIZE,
                ICON_FAV_SIZE,
                false
            )
        busFavoriteMarker = MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(busFavoriteBitmap))
            .anchor(0.5f, 0.5f)


        val tramDrawable = resources.getDrawable(R.drawable.marker_tram, null) as BitmapDrawable
        val tramBitmap = Bitmap.createScaledBitmap(
            tramDrawable.bitmap,
            ICON_SIZE,
            ICON_SIZE,
            false
        )
        tramMarker = MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(tramBitmap))
            .anchor(0.5f, 0.5f)

        val tramFavoriteDrawable = resources.getDrawable(R.drawable.marker_tram_favorite, null) as BitmapDrawable
        val tramFavoriteBitmap =
            Bitmap.createScaledBitmap(
                tramFavoriteDrawable.bitmap,
                ICON_FAV_SIZE,
                ICON_FAV_SIZE,
                false
            )
        tramFavoriteMarker = MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(tramFavoriteBitmap))
            .anchor(0.5f, 0.5f)
    }
}