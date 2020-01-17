package com.jorkoh.transportezaragozakt.destinations.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragment
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.destinations.utils.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.utils.inflateLines
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.stop_row.*
import kotlinx.android.synthetic.main.stop_row.view.*

private const val STATE_LAST_SELECTED_ID = "last_selected_id"

// Used to display plain stops on AllStopsFragment RecyclerView
class StopAdapter(
    private val openStop: (StopDetailsFragmentArgs, Array<Pair<View, String>>) -> Unit,
    private val onReadyToTransition: () -> Unit
) : RecyclerView.Adapter<StopAdapter.StopViewHolder>(), Filterable {

    inner class StopViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        val context: Context
            get() = itemView.context

        fun bind(
            stop: Stop,
            openStop: (StopDetailsFragmentArgs, Array<Pair<View, String>>) -> Unit
        ) {
            // Stop type icon
            when (stop.type) {
                StopType.BUS -> {
                    stop_row_type_image.setImageResource(R.drawable.ic_bus_stop)
                    stop_row_type_image.contentDescription = context.getString(R.string.stop_type_bus)
                }
                StopType.TRAM -> {
                    stop_row_type_image.setImageResource(R.drawable.ic_tram_stop)
                    stop_row_type_image.contentDescription = context.getString(R.string.stop_type_tram)
                }
                StopType.RURAL -> {
                    stop_row_type_image.setImageResource(R.drawable.ic_rural_stop)
                    stop_row_type_image.contentDescription = context.getString(R.string.stop_type_rural)
                }
            }
            // Texts
            stop_row_title.text = stop.stopTitle
            stop_row_number.text = stop.number
            stop_row_number.contentDescription = context.getString(R.string.number_template, stop.number)
            // Favorite icon
            if (stop.isFavorite) {
                stop_row_favorite_icon.setImageResource(R.drawable.ic_favorite_black_24dp)
                stop_row_favorite_icon.contentDescription = context.getString(R.string.stop_favorited)
            } else {
                stop_row_favorite_icon.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                stop_row_favorite_icon.contentDescription = context.getString(R.string.stop_not_favorited)
            }
            // Lines
            stop.lines.inflateLines(itemView.stop_row_lines_layout, stop.type, context)
            // Listeners
            itemView.setOnClickListener(DebounceClickListener {
                // Record the selected item so that we can make the item ready before starting the reenter transition.
                lastSelectedId = stop.stopId

                openStop(StopDetailsFragmentArgs(stop.type.name, stop.stopId),
                    arrayOf(
                        stop_row_card to StopDetailsFragment.TRANSITION_NAME_BACKGROUND,
                        stop_row_mirror_body to StopDetailsFragment.TRANSITION_NAME_BODY,
                        stop_row_layout to StopDetailsFragment.TRANSITION_NAME_APPBAR,
                        stop_row_mirror_toolbar to StopDetailsFragment.TRANSITION_NAME_TOOLBAR,
                        stop_row_type_image to StopDetailsFragment.TRANSITION_NAME_IMAGE,
                        stop_row_title to StopDetailsFragment.TRANSITION_NAME_TITLE,
                        stop_row_lines_layout to StopDetailsFragment.TRANSITION_NAME_LINES,

                        stop_row_distance to StopDetailsFragment.TRANSITION_NAME_FIRST_ELEMENT_FIRST_ROW,
                        stop_row_favorite_icon to StopDetailsFragment.TRANSITION_NAME_FIRST_ELEMENT_SECOND_ROW,
                        stop_row_number to StopDetailsFragment.TRANSITION_NAME_SECOND_ELEMENT_SECOND_ROW,
                        stop_row_mirror_fab to StopDetailsFragment.TRANSITION_NAME_FAB
                    ))
            })
        }
    }

    private var lastSelectedId: String? = null

    val expectsTransition: Boolean
        get() = lastSelectedId != null

    private var displayedStops: List<Stop> = listOf()
    private var stopsFull: List<Stop> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        return StopViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.stop_row, parent, false))
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        val stop = displayedStops[position]

        ViewCompat.setTransitionName(holder.stop_row_card, "stop_row_card_${stop.stopId}")
        ViewCompat.setTransitionName(holder.stop_row_mirror_body, "stop_row_mirror_body_${stop.stopId}")
        ViewCompat.setTransitionName(holder.stop_row_layout, "stop_row_layout_${stop.stopId}")
        ViewCompat.setTransitionName(holder.stop_row_mirror_toolbar, "stop_row_toolbar_${stop.stopId}")
        ViewCompat.setTransitionName(holder.stop_row_type_image, "stop_row_type_image_${stop.stopId}")
        ViewCompat.setTransitionName(holder.stop_row_title, "stop_row_title_${stop.stopId}")
        ViewCompat.setTransitionName(holder.stop_row_lines_layout, "stop_row_lines_layout_${stop.stopId}")

        ViewCompat.setTransitionName(holder.stop_row_distance, "stop_row_distance_${stop.stopId}")
        ViewCompat.setTransitionName(holder.stop_row_favorite_icon, "stop_row_favorite_icon_${stop.stopId}")
        ViewCompat.setTransitionName(holder.stop_row_number, "stop_row_number_${stop.stopId}")
        ViewCompat.setTransitionName(holder.stop_row_mirror_fab, "stop_row_mirror_fab_${stop.stopId}")

        holder.bind(stop, openStop)

        if (stop.stopId == lastSelectedId) {
            onReadyToTransition()
            lastSelectedId = null
        }
    }

    fun saveInstanceState(outState: Bundle) {
        lastSelectedId?.let { id ->
            outState.putString(STATE_LAST_SELECTED_ID, id)
        }
    }

    fun restoreInstanceState(state: Bundle) {
        if (lastSelectedId == null && state.containsKey(STATE_LAST_SELECTED_ID)) {
            lastSelectedId = state.getString(STATE_LAST_SELECTED_ID)
        }
    }

    override fun getItemCount(): Int = displayedStops.size

    fun setNewStops(newStops: List<Stop>, query : String?) {
        stopsFull = newStops
        filter.filter(query)
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
                        (it.number + it.stopTitle).contains(
                            filterPattern,
                            ignoreCase = true
                        )
                    }
                }
                @Suppress("UNCHECKED_CAST")
                //Flag to control whether the recycler view should scroll to the top
                count = if ((values as List<Stop>).count() != displayedStops.count()) 1 else 0
            }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            @Suppress("UNCHECKED_CAST")
            val filteredStops = results?.values as List<Stop>

            // Using DiffUtil to make the filtering feel more dynamic
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = displayedStops.size

                override fun getNewListSize() = filteredStops.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return displayedStops[oldItemPosition].stopId == filteredStops[newItemPosition].stopId
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return displayedStops[oldItemPosition].type == filteredStops[newItemPosition].type
                            && displayedStops[oldItemPosition].lines == filteredStops[newItemPosition].lines
                            && displayedStops[oldItemPosition].stopTitle == filteredStops[newItemPosition].stopTitle
                            && displayedStops[oldItemPosition].isFavorite == filteredStops[newItemPosition].isFavorite
                }
            })
            displayedStops = filteredStops
            result.dispatchUpdatesTo(this@StopAdapter)
        }

    }
}