package com.jorkoh.transportezaragozakt.destinations.line_details

import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.utils.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.utils.inflateLines
import com.jorkoh.transportezaragozakt.destinations.utils.setColor
import com.jorkoh.transportezaragozakt.destinations.utils.toPx
import kotlinx.android.synthetic.main.line_stop_row.view.*

class StopsByDestinationAdapter(
    private val selectStop: (String) -> Unit
) : RecyclerView.Adapter<StopsByDestinationAdapter.StopViewHolder>() {

    class StopViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(stop: Stop, isFirstItem: Boolean, isLastItem: Boolean, selectStop: (String) -> Unit) {
            itemView.apply {
                // Line stop connectors
                first_connector_line_stop.visibility = if (isFirstItem) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
                second_connector_line_stop.visibility = if (isLastItem) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
                if (isFirstItem || isLastItem) {
                    center_connector_line_stop.updateLayoutParams {
                        width = 26.toPx()
                        height = 26.toPx()
                    }
                    center_stroke_connector_line_stop.updateLayoutParams {
                        width = 30.toPx()
                        height = 30.toPx()
                    }
                } else {
                    center_connector_line_stop.updateLayoutParams {
                        width = 22.toPx()
                        height = 22.toPx()
                    }
                    center_stroke_connector_line_stop.updateLayoutParams {
                        width = 26.toPx()
                        height = 26.toPx()
                    }
                }
                val color = when (stop.type) {
                    StopType.BUS -> R.color.bus_color
                    StopType.TRAM -> R.color.tram_color
                    StopType.RURAL -> R.color.rural_color
                }
                (first_connector_line_stop.background as LayerDrawable).findDrawableByLayerId(R.id.inner_line).setColor(context, color)
                (second_connector_line_stop.background as LayerDrawable).findDrawableByLayerId(R.id.inner_line).setColor(context, color)
                center_connector_line_stop.background.setColor(context, color)

                // Texts
                title_line_stop.text = stop.stopTitle
                number_line_stop.text = stop.number
                number_line_stop.contentDescription = context.getString(R.string.number_template, stop.number)
                // Favorite icon
                if (stop.isFavorite) {
                    favorite_icon_line_stop.setImageResource(R.drawable.ic_favorite_black_24dp)
                    favorite_icon_line_stop.contentDescription = context.getString(R.string.stop_favorited)
                } else {
                    favorite_icon_line_stop.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                    favorite_icon_line_stop.contentDescription = context.getString(R.string.stop_not_favorited)
                }

                stop.lines.inflateLines(itemView.lines_layout_line_stop, stop.type, context)

                setOnClickListener(DebounceClickListener {
                    selectStop(stop.stopId)
                })
            }
        }
    }

    var stops: List<Stop> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        return StopViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.line_stop_row, parent, false))
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        val isFirstItem = position == 0
        val isLastItem = position == stops.size - 1

        holder.bind(stops[position], isFirstItem, isLastItem, selectStop)
    }

    override fun getItemCount(): Int = stops.size

    fun setNewStops(newStops: List<Stop>) {
        stops = newStops
        notifyItemRangeInserted(0, newStops.count())
    }
}