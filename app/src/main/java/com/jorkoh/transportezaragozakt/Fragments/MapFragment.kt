package com.jorkoh.transportezaragozakt.Fragments

import com.jorkoh.transportezaragozakt.R
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.jorkoh.transportezaragozakt.Models.BusStopLocations.BusStopLocationsModel
import com.jorkoh.transportezaragozakt.ViewModels.MapViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.google.android.gms.maps.model.LatLngBounds

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        const val DESTINATION_TAG = "MAP"

        const val MAX_ZOOM = 17.5f
        const val MIN_ZOOM = 12.5f
        const val DEFAULT_ZOOM = 15f
        val ZARAGOZA_BOUNDS = LatLngBounds(
            LatLng(41.63, -0.95), LatLng(41.68, -0.84)
        )

        @JvmStatic
        fun newInstance(): MapFragment =
            MapFragment()
    }

    private val mapVM: MapViewModel by viewModel()

    private val locationsObserver = Observer<BusStopLocationsModel> { value ->
        value?.let {
            value.features.forEach {
                map.addMarker(
                    MarkerOptions().position(
                        LatLng(it.geometry.coordinates.last(), it.geometry.coordinates.first())
                    )
                )
            }
        }
    }

    private lateinit var map: GoogleMap

    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d("TestingStuff", "Map Ready")
        map = googleMap!!

        styleMap()
        mapVM.getStopLocations().observe(this, locationsObserver)
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
        map.setMaxZoomPreference(MAX_ZOOM)
        map.setMinZoomPreference(MIN_ZOOM)
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.setLatLngBoundsForCameraTarget(ZARAGOZA_BOUNDS)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ZARAGOZA_BOUNDS.center, DEFAULT_ZOOM))
    }
}
