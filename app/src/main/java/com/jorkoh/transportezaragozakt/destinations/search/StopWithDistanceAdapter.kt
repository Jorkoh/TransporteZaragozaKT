package com.jorkoh.transportezaragozakt.destinations.search

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.db.StopWithDistance
import com.jorkoh.transportezaragozakt.destinations.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.inflateLines
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.stop_row.*
import kotlinx.android.synthetic.main.stop_row.view.*

// Used to display stops with distance on NearbyStopsFragment RecyclerView
class StopWithDistanceAdapter(
    private val openStop: (StopDetailsFragmentArgs) -> Unit
) : RecyclerView.Adapter<StopWithDistanceAdapter.StopViewHolder>(), Filterable {

    class StopViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        val context: Context
            get() = itemView.context

        fun bind(
            stopWithDistance: StopWithDistance,
            openStop: (StopDetailsFragmentArgs) -> Unit
        ) {
            // Stop type icon
            when (stopWithDistance.stop.type) {
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
            title_text_stop.text = stopWithDistance.stop.stopTitle
            number_text_stop.text = stopWithDistance.stop.number
            number_text_stop.contentDescription = context.getString(R.string.number_template, stopWithDistance.stop.number)
            @SuppressLint("SetTextI18n")
            distance_text_stop.text = "${"%.2f".format(stopWithDistance.distance)} m."
            // Favorite icon
            if (stopWithDistance.stop.isFavorite) {
                favorite_icon_stop.setImageResource(R.drawable.ic_favorite_black_24dp)
                favorite_icon_stop.contentDescription = context.getString(R.string.stop_favorited)
            } else {
                favorite_icon_stop.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                favorite_icon_stop.contentDescription = context.getString(R.string.stop_not_favorited)
            }
            // Lines
            stopWithDistance.stop.lines.inflateLines(
                itemView.lines_layout_stop,
                stopWithDistance.stop.type,
                context
            )
            // Listeners
            itemView.setOnClickListener(DebounceClickListener {
                openStop(
                    StopDetailsFragmentArgs(
                        stopWithDistance.stop.type.name,
                        stopWithDistance.stop.stopId
                    )
                )
            })
        }
    }

    private var displayedStops: List<StopWithDistance> = listOf()
    private var stopsFull: List<StopWithDistance> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stop_row, parent, false)
        return StopViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        holder.bind(displayedStops[position], openStop)
    }

    override fun getItemCount(): Int = displayedStops.size

    // When setting new stops we need to call filter afterwards to see the effects
    fun setNewStops(newStops: List<StopWithDistance>) {
        stopsFull = newStops
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults =
            FilterResults().apply {
                values = if (constraint.isNullOrEmpty()) {
                    stopsFull
                } else {
                    val filterPattern = constraint.toString().trim()
                    // Filtering by number and title
                    stopsFull.filter {
                        (it.stop.number + it.stop.stopTitle).contains(
                            filterPattern,
                            ignoreCase = true
                        )
                    }
                }
                @Suppress("UNCHECKED_CAST")
                // Flag to control whether the recycler view should scroll to the top
                count = if ((values as List<Stop>).count() != displayedStops.count()) 1 else 0
            }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            @Suppress("UNCHECKED_CAST")
            val filteredStops = results?.values as List<StopWithDistance>

            // Using DiffUtil to make the filtering feel more dynamic
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = displayedStops.size

                override fun getNewListSize() = filteredStops.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return displayedStops[oldItemPosition].stop.stopId == filteredStops[newItemPosition].stop.stopId
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return displayedStops[oldItemPosition].stop.type == filteredStops[newItemPosition].stop.type
                            && displayedStops[oldItemPosition].stop.lines == filteredStops[newItemPosition].stop.lines
                            && displayedStops[oldItemPosition].stop.stopTitle == filteredStops[newItemPosition].stop.stopTitle
                            && displayedStops[oldItemPosition].distance == filteredStops[newItemPosition].distance
                            && displayedStops[oldItemPosition].stop.isFavorite == filteredStops[newItemPosition].stop.isFavorite
                }
            })
            displayedStops = filteredStops
            result.dispatchUpdatesTo(this@StopWithDistanceAdapter)
        }
    }
}