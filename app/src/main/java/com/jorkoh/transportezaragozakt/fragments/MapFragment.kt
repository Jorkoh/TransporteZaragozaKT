package com.jorkoh.transportezaragozakt.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.activities.MainActivity
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.view_models.MapViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.jorkoh.transportezaragozakt.db.TagInfo
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        const val DESTINATION_TAG = "MAP"

        const val ICON_SIZE = 55
        const val ICON_FAV_SIZE = 70
        const val MAX_ZOOM = 17.5f
        const val MIN_ZOOM = 12f
        const val DEFAULT_ZOOM = 15f
        val ZARAGOZA_BOUNDS = LatLngBounds(
            LatLng(41.6078, -0.9786), LatLng(41.6969, -0.8003)
        )

        @JvmStatic
        fun newInstance(): MapFragment =
            MapFragment()
    }

    private val mapVM: MapViewModel by viewModel()

    private lateinit var busMarker: MarkerOptions
    private lateinit var busFavoriteMarker: MarkerOptions
    private lateinit var tramMarker: MarkerOptions
    private lateinit var tramFavoriteMarker: MarkerOptions

    private val mapStopsMarkers = mutableMapOf<String, Marker>()

    private val stopLocationsObserver: (List<Stop>?) -> Unit = { value: List<Stop>? ->
        value?.let { stops ->
            stops.forEach { stop ->
                val baseMarker = when (stop.type) {
                    StopType.BUS -> if (stop.isFavorite) busFavoriteMarker else busMarker
                    StopType.TRAM -> if (stop.isFavorite) tramFavoriteMarker else tramMarker
                }

                val newMarker = map.addMarker(baseMarker.title(stop.title).position(stop.location))
                newMarker.tag = TagInfo(stop.id, stop.type)
                if (mapStopsMarkers[stop.id]?.isInfoWindowShown == true) {
                    newMarker.showInfoWindow()
                }
                mapStopsMarkers[stop.id]?.remove()
                mapStopsMarkers[stop.id] = newMarker
            }
        }
    }

    private val onInfoWindowClickListener = GoogleMap.OnInfoWindowClickListener { marker ->
        marker?.let {
            val markerInfo = marker.tag
            if (markerInfo is TagInfo) {
                (activity as MainActivity).openStopDetails(markerInfo)
            }
        }
    }

    private lateinit var map: GoogleMap

    override fun onMapReady(googleMap: GoogleMap?) {
        map = checkNotNull(googleMap)

        styleMap()
        map.setOnInfoWindowClickListener(onInfoWindowClickListener)
        mapVM.getBusStopLocations().observe(this, Observer(stopLocationsObserver))
        mapVM.getTramStopLocations().observe(this, Observer(stopLocationsObserver))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapVM.init()
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
        var mapFragment = childFragmentManager.findFragmentByTag("mapFragment") as SupportMapFragment?
        if (mapFragment == null) {
            mapFragment = SupportMapFragment()
            childFragmentManager.beginTransaction()
                .add(R.id.map_fragment_container, mapFragment, "mapFragment")
                .commit()
            childFragmentManager.executePendingTransactions()
        }
        mapFragment.getMapAsync(this)
    }

    private fun styleMap() {
        createBaseMarkers()
        setStyle()

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
                    ZARAGOZA_BOUNDS.center,
                    DEFAULT_ZOOM
                )
            )
            mapVM.mapHasBeenStyled = true
        }

        runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION) {
            @SuppressLint("MissingPermission")
            map.isMyLocationEnabled = true
        }
    }

    private fun createBaseMarkers() {
        val busDrawable = resources.getDrawable(R.drawable.marker_bus, null) as BitmapDrawable
        val busBitmap = Bitmap.createScaledBitmap(busDrawable.bitmap, ICON_SIZE, ICON_SIZE, false)
        busMarker = MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(busBitmap))
            .anchor(0.5f, 0.5f)

        val busFavoriteDrawable = resources.getDrawable(R.drawable.marker_bus_favorite, null) as BitmapDrawable
        val busFavoriteBitmap =
            Bitmap.createScaledBitmap(busFavoriteDrawable.bitmap, ICON_FAV_SIZE, ICON_FAV_SIZE, false)
        busFavoriteMarker = MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(busFavoriteBitmap))
            .anchor(0.5f, 0.5f)


        val tramDrawable = resources.getDrawable(R.drawable.marker_tram, null) as BitmapDrawable
        val tramBitmap = Bitmap.createScaledBitmap(tramDrawable.bitmap, ICON_SIZE, ICON_SIZE, false)
        tramMarker = MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(tramBitmap))
            .anchor(0.5f, 0.5f)

        val tramFavoriteDrawable = resources.getDrawable(R.drawable.marker_tram_favorite, null) as BitmapDrawable
        val tramFavoriteBitmap =
            Bitmap.createScaledBitmap(tramFavoriteDrawable.bitmap, ICON_FAV_SIZE, ICON_FAV_SIZE, false)
        tramFavoriteMarker = MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(tramFavoriteBitmap))
            .anchor(0.5f, 0.5f)
    }

    private fun setStyle() {
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
    }
}