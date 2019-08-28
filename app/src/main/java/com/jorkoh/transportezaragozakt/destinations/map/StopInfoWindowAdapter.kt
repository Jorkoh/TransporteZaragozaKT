package com.jorkoh.transportezaragozakt.destinations.map

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.jorkoh.transportezaragozakt.R
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
        if (marker == null) return null

        val stop = marker.tag as Stop
        val content = layoutInflater.inflate(R.layout.map_info_window, null)

        content.type_image_info_window.setImageResource(
            when (stop.type) {
                StopType.BUS -> R.drawable.ic_bus
                StopType.TRAM -> R.drawable.ic_tram
            }
        )
        content.type_image_info_window.contentDescription =
            when (stop.type) {
                StopType.BUS -> context.getString(R.string.stop_type_bus)
                StopType.TRAM -> context.getString(R.string.stop_type_tram)
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
}