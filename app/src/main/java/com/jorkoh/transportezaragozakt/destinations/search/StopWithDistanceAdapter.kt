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

class StopWithDistanceAdapter(
    private val openStop: (StopDetailsFragmentArgs) -> Unit
) : RecyclerView.Adapter<StopWithDistanceAdapter.StopViewHolder>(), Filterable {

    class StopViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            stop: StopWithDistance,
            openStop: (StopDetailsFragmentArgs) -> Unit
        ) {
            itemView.apply {
                type_image_stop.setImageResource(
                    when (stop.stop.type) {
                        StopType.BUS -> R.drawable.ic_bus
                        StopType.TRAM -> R.drawable.ic_tram
                    }
                )
                title_text_stop.text = stop.stop.stopTitle
                number_text_stop.text = stop.stop.number

                @SuppressLint("SetTextI18n")
                distance_text_stop.text = "${"%.2f".format(stop.distance)} m."

                itemView.lines_layout_stop.removeAllViews()
                val layoutInflater = LayoutInflater.from(context)
                stop.stop.lines.forEachIndexed { index, line ->
                    layoutInflater.inflate(R.layout.map_info_window_line, itemView.lines_layout_stop)
                    val lineView = itemView.lines_layout_stop.getChildAt(index) as TextView

                    val lineColor = if (stop.stop.type == StopType.BUS) R.color.bus_color else R.color.tram_color
                    lineView.background.setColorFilter(
                        ContextCompat.getColor(context, lineColor),
                        PorterDuff.Mode.SRC_IN
                    )
                    lineView.text = line
                }

                setOnClickListener { openStop(StopDetailsFragmentArgs(stop.stop.type.name, stop.stop.stopId)) }
            }
        }
    }

    private var displayedStops: List<StopWithDistance> = listOf()
    private var stopsFull: List<StopWithDistance> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stop_row, parent, false) as View
        return StopViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        holder.bind(displayedStops[position], openStop)
    }

    override fun getItemCount(): Int = displayedStops.size

    //When setting new stops we need to call filter afterwards to see the effects
    fun setNewStops(newStops: List<StopWithDistance>) {
        stopsFull = ArrayList(newStops)
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults = FilterResults().apply {
            values = if (constraint.isNullOrEmpty()) {
                stopsFull
            } else {
                val filterPattern = constraint.toString().trim()
                stopsFull.filter { (it.stop.number + it.stop.stopTitle).contains(filterPattern, ignoreCase = true) }
            }
            @Suppress("UNCHECKED_CAST")
            //Flag to control wheter the recycler view should scroll to the top
            count = if ((values as List<Stop>).count() != displayedStops.count()) 1 else 0
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            @Suppress("UNCHECKED_CAST")
            val filteredStops = results?.values as List<StopWithDistance>

            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = displayedStops.size

                override fun getNewListSize() = filteredStops.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return displayedStops[oldItemPosition].stop.stopId == filteredStops[newItemPosition].stop.stopId
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return displayedStops[oldItemPosition].stop.type == filteredStops[newItemPosition].stop.type
                            && displayedStops[oldItemPosition].stop.lines == filteredStops[newItemPosition].stop.lines
                            && displayedStops[oldItemPosition].stop.stopTitle == filteredStops[newItemPosition].stop.stopTitle
                            && displayedStops[oldItemPosition].distance == filteredStops[newItemPosition].distance
                }
            })
            displayedStops = filteredStops
            result.dispatchUpdatesTo(this@StopWithDistanceAdapter)
        }

    }
}