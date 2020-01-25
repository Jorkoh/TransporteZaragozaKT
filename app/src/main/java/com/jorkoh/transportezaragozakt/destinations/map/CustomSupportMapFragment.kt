package com.jorkoh.transportezaragozakt.destinations.map

import android.graphics.Bitmap
import android.graphics.Point
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
import com.jorkoh.transportezaragozakt.destinations.utils.RemoveFakeTransitionViewEvent
import kotlinx.android.synthetic.main.map_extra_controls.*
import kotlinx.android.synthetic.main.map_extra_controls.view.*
import kotlinx.android.synthetic.main.map_fake_transition_background.view.*
import kotlinx.android.synthetic.main.map_filters.*
import kotlinx.android.synthetic.main.map_filters.view.*
import kotlinx.android.synthetic.main.map_trackings_control.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
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

    private var fakeTransitionView: FakeTransitionInfoWindow? = null

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

    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, savedInstanceState: Bundle?): View? {
        mapViewWrapper = FrameLayout(layoutInflater.context)

        val mapView = super.onCreateView(layoutInflater, viewGroup, savedInstanceState)
        mapView?.setPadding(0, 0, 0, bottomPadding)
        mapViewWrapper?.addView(mapView)

        if (displayFilters) {
            setupFilters(layoutInflater)
        }
        if (displayTrackingsButton) {
            setupTrackerControl(layoutInflater)
        }
        setupExtraMapControls(layoutInflater)
        setupFakeTransitionBackground(layoutInflater)

        fakeTransitionView?.let {
            addFakeTransitionViews(it)
        }
        return mapViewWrapper
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

    private fun setupFilters(layoutInflater: LayoutInflater) {
        val filterChipsView = layoutInflater.inflate(R.layout.map_filters, mapViewWrapper, false)
        filterChipsView.bus_chip.setOnClickListener {
            mapSettingsVM.setBusFilterEnabled(it.bus_chip.isChecked)
        }
        filterChipsView.tram_chip.setOnClickListener {
            mapSettingsVM.setTramFilterEnabled(it.tram_chip.isChecked)
        }
        filterChipsView.rural_chip.setOnClickListener {
            mapSettingsVM.setRuralFilterEnabled(it.rural_chip.isChecked)
        }
        mapViewWrapper?.addView(filterChipsView)
    }

    private fun setupTrackerControl(layoutInflater: LayoutInflater) {
        val mapTrackerControl = layoutInflater.inflate(R.layout.map_trackings_control, mapViewWrapper, false)
        mapViewWrapper?.addView(mapTrackerControl)
        mapViewWrapper?.map_trackings_layout?.updateLayoutParams<FrameLayout.LayoutParams> {
            this@updateLayoutParams.bottomMargin += this@CustomSupportMapFragment.bottomPadding
        }
    }

    private fun setupExtraMapControls(layoutInflater: LayoutInflater) {
        val mapExtraControls = layoutInflater.inflate(R.layout.map_extra_controls, mapViewWrapper, false)
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
        mapViewWrapper?.addView(mapExtraControls)
        mapViewWrapper?.map_types_layout?.updateLayoutParams<FrameLayout.LayoutParams> {
            this@updateLayoutParams.bottomMargin = this@CustomSupportMapFragment.bottomPadding
        }
    }

    private fun setupFakeTransitionBackground(layoutInflater: LayoutInflater) {
        mapViewWrapper?.addView(layoutInflater.inflate(R.layout.map_fake_transition_background, mapViewWrapper, false))
        mapViewWrapper?.map_fake_transition_background_image?.updateLayoutParams<FrameLayout.LayoutParams> {
            this@updateLayoutParams.bottomMargin = this@CustomSupportMapFragment.bottomPadding
        }
    }

    fun addFakeTransitionViews(fakeView: FakeTransitionInfoWindow, mapSnapShot: Bitmap? = null) {
        if (mapSnapShot != null) {
            mapViewWrapper?.map_fake_transition_background_image?.setImageBitmap(mapSnapShot)
        }

        // Measure the view to calculate the margins
        val size = Point()
        requireActivity().windowManager.defaultDisplay.getSize(size)
        fakeView.first.measure(size.x, size.y)

        // Position it according to the screen position of the marker
        mapViewWrapper?.addView(fakeView.first)
        fakeView.first.updateLayoutParams<FrameLayout.LayoutParams> {
            width = FrameLayout.LayoutParams.WRAP_CONTENT
            height = FrameLayout.LayoutParams.WRAP_CONTENT
            leftMargin = fakeView.second.x - fakeView.first.measuredWidth / 2
            val bottomOffset = 55 + (if (fakeView.third) 15 else 0) + 31
            topMargin = fakeView.second.y - fakeView.first.measuredHeight - bottomOffset
        }

        fakeTransitionView = fakeView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid leaks
        mapViewWrapper?.removeAllViews()
        mapViewWrapper = null
    }

    // EventBus to handle removal of fake transition view, event sent from StopDetailsFragment
    @Subscribe
    fun onMessageEvent(event: RemoveFakeTransitionViewEvent) {
        mapViewWrapper?.removeView(fakeTransitionView?.first)
        fakeTransitionView = null
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}

// View, marker screen location and whether the stop is favorite
typealias FakeTransitionInfoWindow = Triple<View, Point, Boolean>