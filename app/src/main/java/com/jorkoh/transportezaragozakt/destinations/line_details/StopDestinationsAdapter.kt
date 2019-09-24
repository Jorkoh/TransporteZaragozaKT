package com.jorkoh.transportezaragozakt.destinations.line_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.inflateLines
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
                // Stop type icon
                when (stop.type) {
                    StopType.BUS -> {
                        type_image_stop.setImageResource(R.drawable.ic_bus_stop)
                        type_image_stop.contentDescription = context.getString(R.string.stop_type_bus)
                    }
                    StopType.TRAM -> {
                        type_image_stop.setImageResource(R.drawable.ic_tram_stop)
                        type_image_stop.contentDescription = context.getString(R.string.stop_type_tram)
                    }
                }
                // Texts
                title_text_stop.text = stop.stopTitle
                number_text_stop.text = stop.number
                number_text_stop.contentDescription = context.getString(R.string.number_template, stop.number)
                // Favorite icon
                if (stop.isFavorite){
                    favorite_icon_stop.setImageResource(R.drawable.ic_favorite_black_24dp)
                    favorite_icon_stop.contentDescription = context.getString(R.string.stop_favorited)
                }else{
                    favorite_icon_stop.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                    favorite_icon_stop.contentDescription = context.getString(R.string.stop_not_favorited)
                }

                stop.lines.inflateLines(itemView.lines_layout_stop, stop.type, context)

                setOnClickListener(DebounceClickListener {
                    selectStop(stop.stopId)
                })
            }
        }
    }

    private var stops: List<Stop> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stop_row, parent, false) as View
        return StopViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        holder.bind(stops[position], selectStop)
    }

    override fun getItemCount(): Int = stops.size

    fun setNewStops(newStops: List<Stop>) {
        stops = newStops
        notifyItemRangeInserted(0, newStops.count())
    }
}