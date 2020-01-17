package com.jorkoh.transportezaragozakt.destinations.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.ViewCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.utils.inflateLines
import kotlinx.android.synthetic.main.map_info_window.view.*
import kotlinx.android.synthetic.main.map_info_window_transition.view.*

class CustomInfoWindowAdapter(val context: Context) : GoogleMap.InfoWindowAdapter {

    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }

    override fun getInfoContents(marker: Marker?): View? {
        val item = marker?.tag
        return if (item is CustomClusterItem) {
            if (item.type.isStop()) {
                inflateStopInfoContents(requireNotNull(item.stop))
            } else {
                inflateTrackingInfoContents(requireNotNull(item.ruralTracking))
            }
        } else {
            null
        }
    }

    private fun inflateStopInfoContents(stop: Stop): View {
        return layoutInflater.inflate(R.layout.map_info_window, null).apply {
            when (stop.type) {
                StopType.BUS -> {
                    map_info_window_type_image.setImageResource(R.drawable.ic_bus_stop)
                    map_info_window_type_image.contentDescription = context.getString(R.string.stop_type_bus)
                }
                StopType.TRAM -> {
                    map_info_window_type_image.setImageResource(R.drawable.ic_tram_stop)
                    map_info_window_type_image.contentDescription = context.getString(R.string.stop_type_tram)
                }
                StopType.RURAL -> {
                    map_info_window_type_image.setImageResource(R.drawable.ic_rural_stop)
                    map_info_window_type_image.contentDescription = context.getString(R.string.stop_type_rural)
                }
            }

            // If the stopTitle is longer we can fit more lines while keeping a nice ratio
            map_info_window_lines_layout.columnCount = when {
                stop.stopTitle.length >= 24 -> 8
                stop.stopTitle.length >= 18 -> 6
                else -> 4
            }

            stop.lines.inflateLines(map_info_window_lines_layout, stop.type, context)
            map_info_window_number.text = stop.number
            map_info_window_title.text = stop.stopTitle
        }
    }

    private fun inflateTrackingInfoContents(tracking: RuralTracking): View? {
        return layoutInflater.inflate(R.layout.map_info_window, null).apply {
            map_info_window_type_image.setImageResource(R.drawable.ic_rural_tracking)
            map_info_window_type_image.contentDescription = context.getString(R.string.rural_tracking)

            // If the stopTitle is longer we can fit more lines while keeping a nice ratio
            map_info_window_lines_layout.columnCount = when {
                tracking.lineId.length >= 24 -> 8
                tracking.lineId.length >= 18 -> 6
                else -> 4
            }

            listOf(tracking.lineId).inflateLines(map_info_window_lines_layout, StopType.RURAL, context)
            map_info_window_number.text = tracking.vehicleId
            map_info_window_title.text = tracking.lineName
        }
    }

    fun inflateFakeTransitionInfoWindow(stop: Stop): View {
        return layoutInflater.inflate(R.layout.map_info_window_transition, null).apply {
            when (stop.type) {
                StopType.BUS -> {
                    map_info_window_transition_type_image.setImageResource(R.drawable.ic_bus_stop)
                }
                StopType.TRAM -> {
                    map_info_window_transition_type_image.setImageResource(R.drawable.ic_tram_stop)
                }
                StopType.RURAL -> {
                    map_info_window_transition_type_image.setImageResource(R.drawable.ic_rural_stop)
                }
            }

            // If the stopTitle is longer we can fit more lines while keeping a nice ratio
            map_info_window_transition_lines_layout.columnCount = when {
                stop.stopTitle.length >= 24 -> 8
                stop.stopTitle.length >= 18 -> 6
                else -> 4
            }

            stop.lines.inflateLines(map_info_window_transition_lines_layout, stop.type, context)
            map_info_window_transition_number.text = stop.number
            map_info_window_transition_title.text = stop.stopTitle

            ViewCompat.setTransitionName(map_info_window_transition_card, "map_info_window_transition_card")
            ViewCompat.setTransitionName(map_info_window_transition_mirror_body, "map_info_window_transition_mirror_body")
            ViewCompat.setTransitionName(map_info_window_transition_layout, "map_info_window_transition_layout")
            ViewCompat.setTransitionName(map_info_window_transition_mirror_toolbar, "map_info_window_transition_mirror_toolbar")
            ViewCompat.setTransitionName(map_info_window_transition_type_image, "map_info_window_transition_type_image")
            ViewCompat.setTransitionName(map_info_window_transition_title, "map_info_window_transition_title")
            ViewCompat.setTransitionName(map_info_window_transition_lines_layout, "map_info_window_transition_lines_layout")
            ViewCompat.setTransitionName(map_info_window_transition_mirror_fab, "map_info_window_transition_mirror_fab")

            ViewCompat.setTransitionName(map_info_window_transition_number, "map_info_window_number")
        }
    }
}
