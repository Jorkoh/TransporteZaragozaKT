package com.jorkoh.transportezaragozakt.destinations.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.SupportMapFragment
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import com.google.android.gms.maps.GoogleMap
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.destinations.line_details.toPx
import kotlinx.android.synthetic.main.map_extra_controls.*
import kotlinx.android.synthetic.main.map_filters.view.*
import kotlinx.android.synthetic.main.map_extra_controls.view.*
import kotlinx.android.synthetic.main.map_filters.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class CustomSupportMapFragment : SupportMapFragment() {

    companion object {
        const val DISPLAY_FILTERS_KEY = "DISPLAY_FILTERS_KEY"
        const val BOTTOM_MARGIN_KEY = "BOTTOM_MARGIN_KEY"

        fun newInstance(displayFilters: Boolean = true, bottomMargin: Boolean = false): CustomSupportMapFragment {
            val instance = CustomSupportMapFragment()
            instance.arguments = Bundle().apply {
                putBoolean(DISPLAY_FILTERS_KEY, displayFilters)
                putBoolean(BOTTOM_MARGIN_KEY, bottomMargin)
            }
            return instance
        }
    }

    private var displayFilters: Boolean = true
    private var bottomMargin: Boolean = false

    private val mapVM: MapViewModel by sharedViewModel()

    private val busFilterEnabledObserver = Observer<Boolean> { enabled ->
        bus_chip.isChecked = enabled
    }

    private val tramFilterEnabledObserver = Observer<Boolean> { enabled ->
        tram_chip.isChecked = enabled
    }

    private val mapTypesObserver = Observer<Int> { mapType ->
        when (mapType) {
            GoogleMap.MAP_TYPE_NORMAL -> map_type_button.setImageResource(R.drawable.ic_satellite_black_24dp)
            GoogleMap.MAP_TYPE_SATELLITE -> map_type_button.setImageResource(R.drawable.ic_map_black_24dp)
        }
    }

    private val trafficObserver = Observer<Boolean> { enabled ->
        traffic_button.setImageResource(if (enabled) R.drawable.ic_layers_clear_black_24dp else R.drawable.ic_traffic_black_24dp)
    }

    override fun onCreate(p0: Bundle?) {
        super.onCreate(p0)
        displayFilters = arguments?.getBoolean(DISPLAY_FILTERS_KEY) ?: true
        bottomMargin = arguments?.getBoolean(BOTTOM_MARGIN_KEY) ?: false
    }

    override fun onActivityCreated(bundle: Bundle?) {
        super.onActivityCreated(bundle)

        if (displayFilters) {
            mapVM.busFilterEnabled.observe(viewLifecycleOwner, busFilterEnabledObserver)
            mapVM.tramFilterEnabled.observe(viewLifecycleOwner, tramFilterEnabledObserver)
        }
        mapVM.mapType.observe(viewLifecycleOwner, mapTypesObserver)
        mapVM.trafficEnabled.observe(viewLifecycleOwner, trafficObserver)
    }

    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, savedState: Bundle?): View? {
        val wrapper = FrameLayout(layoutInflater.context)

        val mapView = super.onCreateView(layoutInflater, viewGroup, savedState)
        wrapper.addView(mapView)


        if (displayFilters) {
            setupFilters(layoutInflater, wrapper)
        }

        setupExtraMapControls(layoutInflater, wrapper)

        return wrapper
    }

    private fun setupFilters(layoutInflater: LayoutInflater, wrapper: FrameLayout) {
        val filterChipsView = layoutInflater.inflate(R.layout.map_filters, wrapper, false)
        filterChipsView.bus_chip.setOnClickListener {
            mapVM.setBusFilterEnabled(it.bus_chip.isChecked)
        }
        filterChipsView.tram_chip.setOnClickListener {
            mapVM.setTramFilterEnabled(it.tram_chip.isChecked)
        }
        wrapper.addView(filterChipsView)
    }

    private fun setupExtraMapControls(layoutInflater: LayoutInflater, wrapper: FrameLayout) {
        val mapExtraControls = layoutInflater.inflate(R.layout.map_extra_controls, wrapper, false)
        mapExtraControls.map_type_button.setOnClickListener {
            if (mapVM.mapType.value == GoogleMap.MAP_TYPE_NORMAL) {
                mapVM.setMapType(GoogleMap.MAP_TYPE_SATELLITE)
            } else {
                mapVM.setMapType(GoogleMap.MAP_TYPE_NORMAL)
            }
        }
        mapExtraControls.traffic_button.setOnClickListener {
            mapVM.setTrafficEnabled(mapVM.trafficEnabled.value != true)
        }
        wrapper.addView(mapExtraControls)
        if (bottomMargin) {
            wrapper.map_types_map.updateLayoutParams<FrameLayout.LayoutParams> {
                bottomMargin = 160.toPx()
            }
        }
    }

}