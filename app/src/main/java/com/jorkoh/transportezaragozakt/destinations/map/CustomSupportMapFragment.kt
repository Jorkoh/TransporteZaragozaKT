package com.jorkoh.transportezaragozakt.destinations.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.SupportMapFragment
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.filter_chips_map.view.*
import kotlinx.android.synthetic.main.map_types_map.view.*
import kotlinx.android.synthetic.main.notification_custom.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class CustomSupportMapFragment : SupportMapFragment() {

    private val mapVM: MapViewModel by sharedViewModel()

    private val busFilterEnabledObserver: (Boolean) -> Unit = { enabled ->
        updateFiltersUI(enabled, null, view)
    }

    private val tramFilterEnabledObserver: (Boolean) -> Unit = { enabled ->
        updateFiltersUI(null, enabled, view)
    }

    private val mapTypesObserver: (Int) -> Unit = { enabled ->
        updateMapTypesUI(enabled, null, view)
    }

    private val trafficObserver: (Boolean) -> Unit = { enabled ->
        updateMapTypesUI(null, enabled, view)
    }

    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, savedState: Bundle?): View? {
        val wrapper = FrameLayout(layoutInflater.context)

        val mapView = super.onCreateView(layoutInflater, viewGroup, savedState)
        wrapper.addView(mapView)

        setupFiltersControl(layoutInflater, wrapper)
        setupMapTypesControl(layoutInflater, wrapper)

        return wrapper
    }

    private fun setupFiltersControl(layoutInflater: LayoutInflater, wrapper: FrameLayout) {
        val filterChipsView = layoutInflater.inflate(R.layout.filter_chips_map, wrapper, false)
        filterChipsView.bus_chip.setOnClickListener {
            mapVM.setBusFilterEnabled(it.bus_chip.isChecked)
        }
        filterChipsView.tram_chip.setOnClickListener {
            mapVM.setTramFilterEnabled(it.tram_chip.isChecked)
        }
        wrapper.addView(filterChipsView)

        updateFiltersUI(mapVM.getBusFilterEnabled().value, mapVM.getTramFilterEnabled().value, wrapper)
        mapVM.getBusFilterEnabled().observe(this, Observer(busFilterEnabledObserver))
        mapVM.getTramFilterEnabled().observe(this, Observer(tramFilterEnabledObserver))
    }

    private fun setupMapTypesControl(layoutInflater: LayoutInflater, wrapper: FrameLayout) {
        //TODO:  RENAME THIS STUFF, IS MORE THAN MAP TYPES
        val mapTypesView = layoutInflater.inflate(R.layout.map_types_map, wrapper, false)
        mapTypesView.map_type_button.setOnClickListener {
            if (mapVM.getMapType().value == GoogleMap.MAP_TYPE_NORMAL) {
                mapVM.setMapType(GoogleMap.MAP_TYPE_SATELLITE)
            } else {
                mapVM.setMapType(GoogleMap.MAP_TYPE_NORMAL)
            }
        }
        mapTypesView.traffic_button.setOnClickListener {
            mapVM.setTrafficEnabled(mapVM.getTrafficEnabled().value != true)
        }
        wrapper.addView(mapTypesView)

        updateMapTypesUI(mapVM.getMapType().value, mapVM.getTrafficEnabled().value, wrapper)
        mapVM.getMapType().observe(this, Observer(mapTypesObserver))
        mapVM.getTrafficEnabled().observe(this, Observer(trafficObserver))
    }

    private fun updateFiltersUI(isBusFilterEnabled: Boolean?, isTramFilterEnabled: Boolean?, rootView: View?) {
        rootView?.let {
            if (isBusFilterEnabled != null) {
                it.bus_chip.isChecked = isBusFilterEnabled
            }
            if (isTramFilterEnabled != null) {
                it.tram_chip.isChecked = isTramFilterEnabled
            }
        }
    }

    private fun updateMapTypesUI(mapType: Int?, trafficEnabled: Boolean?, rootView: View?) {
        rootView?.let {
            if (mapType != null) {
                when (mapType) {
                    GoogleMap.MAP_TYPE_NORMAL -> it.map_type_button.setImageResource(R.drawable.ic_satellite_black_24dp)
                    GoogleMap.MAP_TYPE_SATELLITE -> it.map_type_button.setImageResource(R.drawable.ic_map_black_24dp)
                }
            }
            if (trafficEnabled != null) {
                it.traffic_button.setImageResource(if (trafficEnabled) R.drawable.ic_layers_clear_black_24dp else R.drawable.ic_traffic_black_24dp)
            }
        }
    }
}