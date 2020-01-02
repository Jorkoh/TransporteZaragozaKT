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
import kotlinx.android.synthetic.main.map_extra_controls.*
import kotlinx.android.synthetic.main.map_extra_controls.view.*
import kotlinx.android.synthetic.main.map_filters.*
import kotlinx.android.synthetic.main.map_filters.view.*
import kotlinx.android.synthetic.main.map_trackings_control.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class CustomSupportMapFragment : SupportMapFragment() {

    companion object {
        const val DISPLAY_FILTERS_KEY = "DISPLAY_FILTERS_KEY"
        const val DISPLAY_TRACKINGS_BUTTON_KEY = "DISPLAY_TRACKINGS_BUTTON_KEY"
        const val BOTTOM_PADDING_DIMEN_KEY = "BOTTOM_PADDING_DIMEN_KEY"

        fun newInstance(
            displayFilters: Boolean = true,
            displayTrackingsButton: Boolean = true,
            bottomPaddingDimen: Int = 0
        ): CustomSupportMapFragment {
            val instance = CustomSupportMapFragment()
            instance.arguments = Bundle().apply {
                putBoolean(DISPLAY_FILTERS_KEY, displayFilters)
                putBoolean(DISPLAY_TRACKINGS_BUTTON_KEY, displayTrackingsButton)
                putInt(BOTTOM_PADDING_DIMEN_KEY, bottomPaddingDimen)
            }
            return instance
        }
    }

    private var displayFilters: Boolean = true
    private var displayTrackingsButton: Boolean = true
    private var bottomPadding: Int = 0

    private var mapViewWrapper: FrameLayout? = null

    private val mapSettingsVM: MapSettingsViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayFilters = arguments?.getBoolean(DISPLAY_FILTERS_KEY) ?: true
        displayTrackingsButton = arguments?.getBoolean(DISPLAY_TRACKINGS_BUTTON_KEY) ?: true
        (arguments?.getInt(BOTTOM_PADDING_DIMEN_KEY) ?: 0).takeIf { it != 0 }?.let { bottomPaddingDimen ->
            bottomPadding = resources.getDimensionPixelOffset(bottomPaddingDimen)
        }
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
            mapSettingsVM.ruralFilterEnabled.observe(viewLifecycleOwner, Observer { enabled ->
                rural_chip.isChecked = enabled
            })
        }
        mapSettingsVM.mapType.observe(viewLifecycleOwner, Observer<Int> { mapType ->
            when (mapType) {
                GoogleMap.MAP_TYPE_NORMAL -> {
                    map_type_button.setImageResource(R.drawable.ic_satellite_black_24dp)
                    map_type_button.contentDescription = getString(R.string.map_type_satellite_description)
                }
                GoogleMap.MAP_TYPE_SATELLITE -> {
                    map_type_button.setImageResource(R.drawable.ic_map_black_24dp)
                    map_type_button.contentDescription = getString(R.string.map_type_normal_description)
                }
            }
        })
        mapSettingsVM.trafficEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            if (enabled) {
                traffic_button.setImageResource(R.drawable.ic_layers_clear_black_24dp)
                traffic_button.contentDescription = getString(R.string.traffic_layer_disabled_description)
            } else {
                traffic_button.setImageResource(R.drawable.ic_traffic_black_24dp)
                traffic_button.contentDescription = getString(R.string.traffic_layer_enabled_description)
            }

        })
    }

    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, savedInstanceState: Bundle?): View? {
        mapViewWrapper = FrameLayout(layoutInflater.context).apply {
            val mapView = super.onCreateView(layoutInflater, viewGroup, savedInstanceState)
            mapView?.setPadding(0, 0, 0, bottomPadding)
            addView(mapView)

            if (displayFilters) {
                setupFilters(layoutInflater, this)
            }
            if (displayTrackingsButton) {
                setupTrackerControl(layoutInflater, this)
            }
            setupExtraMapControls(layoutInflater, this)
        }
        return mapViewWrapper
    }

    private fun setupFilters(layoutInflater: LayoutInflater, wrapper: FrameLayout) {
        val filterChipsView = layoutInflater.inflate(R.layout.map_filters, wrapper, false)
        filterChipsView.bus_chip.setOnClickListener {
            mapSettingsVM.setBusFilterEnabled(it.bus_chip.isChecked)
        }
        filterChipsView.tram_chip.setOnClickListener {
            mapSettingsVM.setTramFilterEnabled(it.tram_chip.isChecked)
        }
        filterChipsView.rural_chip.setOnClickListener {
            mapSettingsVM.setRuralFilterEnabled(it.rural_chip.isChecked)
        }
        wrapper.addView(filterChipsView)
    }

    private fun setupTrackerControl(layoutInflater: LayoutInflater, wrapper: FrameLayout) {
        val mapTrackerControl = layoutInflater.inflate(R.layout.map_trackings_control, wrapper, false)
        wrapper.addView(mapTrackerControl)
        wrapper.map_trackings_layout.updateLayoutParams<FrameLayout.LayoutParams> {
            this@updateLayoutParams.bottomMargin += this@CustomSupportMapFragment.bottomPadding
        }
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
        wrapper.map_types_layout.updateLayoutParams<FrameLayout.LayoutParams> {
            this@updateLayoutParams.bottomMargin = this@CustomSupportMapFragment.bottomPadding
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid leaks
        mapViewWrapper?.removeAllViews()
        mapViewWrapper = null
    }
}