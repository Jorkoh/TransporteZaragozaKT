package com.jorkoh.transportezaragozakt.destinations.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.destinations.toPx
import kotlinx.android.synthetic.main.map_extra_controls.*
import kotlinx.android.synthetic.main.map_extra_controls.view.*
import kotlinx.android.synthetic.main.map_filters.*
import kotlinx.android.synthetic.main.map_filters.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class CustomSupportMapFragment : SupportMapFragment() {

    companion object {
        const val DISPLAY_FILTERS_KEY = "DISPLAY_FILTERS_KEY"
        const val BOTTOM_MARGIN_KEY = "BOTTOM_MARGIN_KEY"

        fun newInstance(displayFilters: Boolean = true, bottomMargin: Int = 0): CustomSupportMapFragment {
            val instance = CustomSupportMapFragment()
            instance.arguments = Bundle().apply {
                putBoolean(DISPLAY_FILTERS_KEY, displayFilters)
                putInt(BOTTOM_MARGIN_KEY, bottomMargin)
            }
            return instance
        }
    }

    private var displayFilters: Boolean = true
    private var bottomMargin: Int = 0

    private val mapSettingsVM: MapSettingsViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayFilters = arguments?.getBoolean(DISPLAY_FILTERS_KEY) ?: true
        bottomMargin = arguments?.getInt(BOTTOM_MARGIN_KEY) ?: 0
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (displayFilters) {
            mapSettingsVM.busFilterEnabled.observe(viewLifecycleOwner, Observer { enabled ->
                bus_chip.isChecked = enabled
            })
            mapSettingsVM.tramFilterEnabled.observe(viewLifecycleOwner, Observer { enabled ->
                tram_chip.isChecked = enabled
            })
        }
        mapSettingsVM.mapType.observe(viewLifecycleOwner, Observer<Int> { mapType ->
            when (mapType) {
                GoogleMap.MAP_TYPE_NORMAL -> map_type_button.setImageResource(R.drawable.ic_satellite_black_24dp)
                GoogleMap.MAP_TYPE_SATELLITE -> map_type_button.setImageResource(R.drawable.ic_map_black_24dp)
            }
        })
        mapSettingsVM.trafficEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            traffic_button.setImageResource(if (enabled) R.drawable.ic_layers_clear_black_24dp else R.drawable.ic_traffic_black_24dp)
        })
    }

    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, savedInstanceState: Bundle?): View? {
        val wrapper = FrameLayout(layoutInflater.context)

        val mapView = super.onCreateView(layoutInflater, viewGroup, savedInstanceState)
        //TODO mapView?.paddingBottom
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
            mapSettingsVM.setBusFilterEnabled(it.bus_chip.isChecked)
        }
        filterChipsView.tram_chip.setOnClickListener {
            mapSettingsVM.setTramFilterEnabled(it.tram_chip.isChecked)
        }
        wrapper.addView(filterChipsView)
    }

    private fun setupExtraMapControls(layoutInflater: LayoutInflater, wrapper: FrameLayout) {
        val mapExtraControls = layoutInflater.inflate(R.layout.map_extra_controls, wrapper, false)
        mapExtraControls.map_type_button.setOnClickListener {
            if (mapSettingsVM.mapType.value == GoogleMap.MAP_TYPE_NORMAL) {
                mapSettingsVM.setMapType(GoogleMap.MAP_TYPE_SATELLITE)
            } else {
                mapSettingsVM.setMapType(GoogleMap.MAP_TYPE_NORMAL)
            }
        }
        mapExtraControls.traffic_button.setOnClickListener {
            mapSettingsVM.setTrafficEnabled(mapSettingsVM.trafficEnabled.value != true)
        }
        wrapper.addView(mapExtraControls)
        if (bottomMargin != 0) {
            wrapper.map_types_map.updateLayoutParams<FrameLayout.LayoutParams> {
                this@updateLayoutParams.bottomMargin = this@CustomSupportMapFragment.bottomMargin.toPx()
            }
        }
    }

}