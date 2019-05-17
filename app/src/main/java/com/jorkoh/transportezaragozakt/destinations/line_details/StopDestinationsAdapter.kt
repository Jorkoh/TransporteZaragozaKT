package com.jorkoh.transportezaragozakt.destinations.line_details

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.location.Location
import android.view.*
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import kotlinx.android.synthetic.main.stop_row.view.*

class StopDestinationsAdapter(
    private val selectStop: (String) -> Unit
) : RecyclerView.Adapter<StopDestinationsAdapter.StopViewHolder>() {

    class StopViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            stop: Stop,
            selectStop: (String) -> Unit
        ) {
            itemView.apply {
                type_image_stop.setImageResource(
                    when (stop.type) {
                        StopType.BUS -> R.drawable.ic_bus
                        StopType.TRAM -> R.drawable.ic_tram
                    }
                )
                title_text_stop.text = stop.stopTitle
                number_text_stop.text = stop.number
                favorite_icon_stop.setImageResource(
                    if (stop.isFavorite) R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp
                )

                itemView.lines_layout_stop.removeAllViews()
                val layoutInflater = LayoutInflater.from(context)
                stop.lines.forEachIndexed { index, line ->
                    layoutInflater.inflate(R.layout.map_info_window_line, itemView.lines_layout_stop)
                    val lineView = itemView.lines_layout_stop.getChildAt(index) as TextView

                    val lineColor = if (stop.type == StopType.BUS) R.color.bus_color else R.color.tram_color
                    lineView.background.setColorFilter(
                        ContextCompat.getColor(context, lineColor),
                        PorterDuff.Mode.SRC_IN
                    )
                    lineView.text = line
                }

                setOnClickListener { selectStop(stop.stopId) }
            }
        }
    }

    private var stops: List<Stop> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        //TODO PROBABLY CHANGE THIS LAYOUT
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stop_row, parent, false) as View
        return StopViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        holder.bind(stops[position], selectStop)
    }

    override fun getItemCount(): Int = stops.size

    fun setNewStops(newStops: List<Stop>) {
        stops = newStops
        notifyItemRangeInserted(0, newStops.count()-1)
    }
}