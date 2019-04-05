package com.jorkoh.transportezaragozakt.destinations.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.SupportMapFragment
import android.widget.FrameLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.map_types_map.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class CustomSupportMapFragment : SupportMapFragment() {

    private val mapVM: MapViewModel by sharedViewModel()

    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, savedState: Bundle?): View? {
        val wrapper = FrameLayout(layoutInflater.context)

        val mapView = super.onCreateView(layoutInflater, viewGroup, savedState)
        wrapper.addView(mapView)

        val filterChipsView = layoutInflater.inflate(R.layout.filter_chips_map, wrapper, false)
        wrapper.addView(filterChipsView)

        val mapTypesView = layoutInflater.inflate(R.layout.map_types_map, wrapper, false)
        mapTypesView.map_type_button.setOnClickListener {
            if (mapVM.getMapType().value == GoogleMap.MAP_TYPE_NORMAL) {
                mapVM.setMapType(GoogleMap.MAP_TYPE_SATELLITE)
                mapTypesView.map_type_button.setImageResource(R.drawable.ic_map_black_24dp)
            } else {
                mapVM.setMapType(GoogleMap.MAP_TYPE_NORMAL)
                mapTypesView.map_type_button.setImageResource(R.drawable.ic_satellite_black_24dp)
            }
        }
        wrapper.addView(mapTypesView)


        return wrapper
    }
}