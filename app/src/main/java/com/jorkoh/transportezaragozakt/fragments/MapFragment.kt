package com.jorkoh.transportezaragozakt.fragments

import com.jorkoh.transportezaragozakt.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.jorkoh.transportezaragozakt.models.Bus.BusStopLocations.BusStopLocationsModel
import com.jorkoh.transportezaragozakt.view_models.MapViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.google.android.gms.maps.model.LatLngBounds
import com.jorkoh.transportezaragozakt.models.Tram.TramStopLocations.TramStopLocationsModel

class MapFragment : Fragment(), OnMapReadyCallback {

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

    private val busLocationsObserver = Observer<BusStopLocationsModel> { value ->
        value?.let {
            value.locations.forEach {
                map.addMarker(MarkerOptions().alpha(0.5f).position(it.geometry.coordinates))
            }
        }
    }

    private val tramLocationsObserver = Observer<TramStopLocationsModel> { value ->
        value?.let {
            value.locations.forEach {
                map.addMarker(MarkerOptions().alpha(1f).position(it.geometry.coordinates))
            }
        }
    }

    private lateinit var map: GoogleMap

    override fun onMapReady(googleMap: GoogleMap?) {
        map = checkNotNull(googleMap)

        styleMap()
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
        map.setMaxZoomPreference(MAX_ZOOM)
        map.setMinZoomPreference(MIN_ZOOM)
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.setLatLngBoundsForCameraTarget(ZARAGOZA_BOUNDS)
        //Don't center the camera when coming back from orientation change
        if (!mapVM.mapHasBeenStyled) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                ZARAGOZA_BOUNDS.center,
                DEFAULT_ZOOM
            ))
            mapVM.mapHasBeenStyled = true
        }
    }
}
