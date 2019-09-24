package com.jorkoh.transportezaragozakt.destinations.map

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.inflateLines
import kotlinx.android.synthetic.main.map_info_window.view.*

class StopInfoWindowAdapter(val context: Context) : GoogleMap.InfoWindowAdapter {

    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }

    @SuppressLint("InflateParams")
    override fun getInfoContents(marker: Marker?): View? {
        val item = marker?.tag
        return if (item is CustomClusterItem && item.type != CustomClusterItem.ClusterItemType.RURAL_TRACKING) {
            if (item.type != CustomClusterItem.ClusterItemType.RURAL_TRACKING) {
                inflateStopInfoContents(requireNotNull(item.stop))
            } else {
                inflateTrackingInfoContents(requireNotNull(item.ruralTracking))
            }
        } else{
            null
        }
    }

    private fun inflateStopInfoContents(stop : Stop) : View {
        val content = layoutInflater.inflate(R.layout.map_info_window, null)

        when (stop.type) {
            StopType.BUS -> {
                content.type_image_info_window.setImageResource(R.drawable.ic_bus)
                content.type_image_info_window.contentDescription = context.getString(R.string.stop_type_bus)
            }
            StopType.TRAM -> {
                content.type_image_info_window.setImageResource(R.drawable.ic_tram)
                content.type_image_info_window.contentDescription = context.getString(R.string.stop_type_tram)
            }
            StopType.RURAL -> {
                content.type_image_info_window.setImageResource(R.drawable.ic_rural)
                content.type_image_info_window.contentDescription = context.getString(R.string.stop_type_rural)
            }
        }

        // If the stopTitle is longer we can fit more lines while keeping a nice ratio
        content.lines_layout_favorite.columnCount = when {
            stop.stopTitle.length >= 24 -> 8
            stop.stopTitle.length >= 18 -> 6
            else -> 4
        }

        stop.lines.inflateLines(content.lines_layout_favorite, stop.type, context)
        content.number_text_info_window.text = stop.number
        content.title_text_info_window.text = stop.stopTitle

        return content
    }

    private fun inflateTrackingInfoContents(ruralTracking: RuralTracking) : View?{
        //TODO
        return null
    }
}
