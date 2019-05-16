package com.jorkoh.transportezaragozakt.destinations.search

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

class StopAdapter(
    private val openStop: (StopDetailsFragmentArgs) -> Unit
) : RecyclerView.Adapter<StopAdapter.StopViewHolder>(), Filterable {

    class StopViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            stop: Stop,
            openStop: (StopDetailsFragmentArgs) -> Unit
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

                setOnClickListener { openStop(StopDetailsFragmentArgs(stop.type.name, stop.stopId)) }
            }
        }
    }

    private var displayedStops: List<Stop> = listOf()
    private var stopsFull: List<Stop> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stop_row, parent, false) as View
        return StopViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        holder.bind(displayedStops[position], openStop)
    }

    override fun getItemCount(): Int = displayedStops.size

    //When setting new stops we need to call filter afterwards to see the effects
    fun setNewStops(newStops: List<Stop>) {
        stopsFull = newStops
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults = FilterResults().apply {
            values = if (constraint.isNullOrEmpty()) {
                stopsFull
            } else {
                val filterPattern = constraint.toString().trim()
                stopsFull.filter { (it.number + it.stopTitle).contains(filterPattern, ignoreCase = true) }
            }
            @Suppress("UNCHECKED_CAST")
            //Flag to control wheter the recycler view should scroll to the top
            count = if ((values as List<Stop>).count() != displayedStops.count()) 1 else 0
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            @Suppress("UNCHECKED_CAST")
            val filteredStops = results?.values as List<Stop>

            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = displayedStops.size

                override fun getNewListSize() = filteredStops.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return displayedStops[oldItemPosition].stopId == filteredStops[newItemPosition].stopId
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return displayedStops[oldItemPosition].type == filteredStops[newItemPosition].type
                            && displayedStops[oldItemPosition].lines == filteredStops[newItemPosition].lines
                            && displayedStops[oldItemPosition].stopTitle == filteredStops[newItemPosition].stopTitle
                }
            })
            displayedStops = filteredStops
            result.dispatchUpdatesTo(this@StopAdapter)
        }

    }
}