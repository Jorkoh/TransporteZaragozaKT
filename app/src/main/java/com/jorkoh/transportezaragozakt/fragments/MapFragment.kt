package com.jorkoh.transportezaragozakt.fragments

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



class MapFragment : Fragment(), OnMapReadyCallback {

    data class TagInfo(val id: String, val type: StopType)

    companion object {
        const val DESTINATION_TAG = "MAP"

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
    private lateinit var tramMarker: MarkerOptions

    private val busLocationsObserver = Observer<List<Stop>> { value ->
        value?.let { stops ->
            stops.forEach { stop ->
                map.addMarker(busMarker.title(stop.title).position(stop.location))
                    .tag = TagInfo(stop.id, StopType.BUS)
            }
        }
    }

    private val tramLocationsObserver = Observer<List<Stop>> { value ->
        value?.let { stops ->
            stops.forEach { stop ->
                map.addMarker(tramMarker.title(stop.title).position(stop.location))
                    .tag = TagInfo(stop.id, StopType.TRAM)
            }
        }
    }

    private val onInfoWindowClickListener = GoogleMap.OnInfoWindowClickListener { marker ->
        marker?.let {
            val markerInfo = marker.tag
            if (markerInfo is TagInfo) {
                (activity as MainActivity).openStopDetails(markerInfo.id, markerInfo.type)
            }
        }
    }

    private lateinit var map: GoogleMap

    override fun onMapReady(googleMap: GoogleMap?) {
        map = checkNotNull(googleMap)

        styleMap()
        map.setOnInfoWindowClickListener(onInfoWindowClickListener)
        mapVM.getBusStopLocations().observe(this, busLocationsObserver)
        mapVM.getTramStopLocations().observe(this, tramLocationsObserver)
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
        val busDrawable = resources.getDrawable(R.drawable.marker_bus, null) as BitmapDrawable
        val busBitmap = Bitmap.createScaledBitmap(busDrawable.bitmap, 48, 48, false)
        busMarker = MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(busBitmap))
            .anchor(0.5f, 0.5f)
        val tramDrawable = resources.getDrawable(R.drawable.marker_tram, null) as BitmapDrawable
        val tramBitmap = Bitmap.createScaledBitmap(tramDrawable.bitmap, 48, 48, false)
        tramMarker = MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(tramBitmap))
            .anchor(0.5f, 0.5f)

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
    }
}