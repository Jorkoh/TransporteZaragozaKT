package com.jorkoh.transportezaragozakt.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.ViewModels.MapViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        const val DESTINATION_TAG = "MAP"

        @JvmStatic
        fun newInstance(): MapFragment =
            MapFragment()
    }

    private val mapVM: MapViewModel by viewModel()

    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d("TestingStuff", "Map Ready")
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
}
