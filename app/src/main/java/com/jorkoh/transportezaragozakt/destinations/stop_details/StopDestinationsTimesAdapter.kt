package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.db.toLineType
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsFragment
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.destinations.utils.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.utils.fixTimes
import com.jorkoh.transportezaragozakt.destinations.utils.toPx
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.destination_row.*

private const val STATE_LAST_SELECTED_ID = "last_selected_id"

// Used to display arrival times on StopDetailsFragment RecyclerView
class StopDestinationsTimesAdapter(
    private val openLine: (LineDetailsFragmentArgs, Array<Pair<View, String>>) -> Unit,
    private val openNotTrackedWarning: () -> Unit,
    private val onReadyToTransition: () -> Unit
) : RecyclerView.Adapter<StopDestinationsTimesAdapter.StopDestinationsViewHolder>() {

    inner class StopDestinationsViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        val context: Context
            get() = itemView.context

        fun bind(
            destination: StopDestination,
            stopType: StopType,
            stopId: String,
            openLine: (LineDetailsFragmentArgs, Array<Pair<View, String>>) -> Unit,
            openNotTrackedWarning: () -> Unit
        ) {
            // Line number and color
            destination_row_line_text.text = destination.line
            destination_row_line_text.contentDescription = context.getString(R.string.line_template, destination.line)
            destination_row_line_text.setBackgroundColor(
                ContextCompat.getColor(
                    context, when (stopType) {
                        StopType.BUS -> R.color.bus_color
                        StopType.TRAM -> R.color.tram_color
                        StopType.RURAL -> R.color.rural_color
                    }
                )
            )
            destination_row_line_text.minWidth = when (stopType) {
                StopType.BUS -> 100.toPx()
                StopType.TRAM -> 100.toPx()
                StopType.RURAL -> 135.toPx()
            }
            // Texts
            destination_text.text = destination.destination
            first_time_text.text = destination.times[0].fixTimes(context)
            second_time_text.text = destination.times[1].fixTimes(context)
            // Warning icons
            first_time_warning.visibility = if (destination.areTrackedTimes[0] == "Y") View.GONE else View.VISIBLE
            second_time_warning.visibility = if (destination.areTrackedTimes[1] == "Y") View.GONE else View.VISIBLE
            // Listeners
            itemView.setOnClickListener(DebounceClickListener {
                // Record the selected item so that we can make the item ready before starting the reenter transition.
                lastSelectedId = destination.line + destination.destination

                openLine(
                    LineDetailsFragmentArgs(stopType.toLineType().name, destination.line, stopId),
                    arrayOf(
                        destination_row_card to LineDetailsFragment.TRANSITION_NAME_BACKGROUND,
                        destination_row_mirror_body to LineDetailsFragment.TRANSITION_NAME_BODY_DETAILS,
                        destination_row_constraint_layout to LineDetailsFragment.TRANSITION_NAME_BODY_ROW

                    )
                )
            })
            first_time_warning.setOnClickListener {
                openNotTrackedWarning()
            }
            second_time_warning.setOnClickListener {
                openNotTrackedWarning()
            }
        }
    }

    private var lastSelectedId: String? = null

    val expectsTransition: Boolean
        get() = lastSelectedId != null

    private var stopDestinations = listOf<StopDestination>()

    lateinit var stopType: StopType
    lateinit var stopId: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopDestinationsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.destination_row, parent, false) as View
        return StopDestinationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopDestinationsViewHolder, position: Int) {
        val destination = stopDestinations[position]

        ViewCompat.setTransitionName(holder.destination_row_card, "destination_row_card_${destination.line+destination.destination}")
        ViewCompat.setTransitionName(holder.destination_row_mirror_body, "destination_row_mirror_body_${destination.line+destination.destination}")
        ViewCompat.setTransitionName(holder.destination_row_constraint_layout, "destination_row_coordinator_layout_${destination.line+destination.destination}")

        holder.bind(stopDestinations[position], stopType, stopId, openLine, openNotTrackedWarning)

        if (destination.line+destination.destination == lastSelectedId) {
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

    override fun getItemCount(): Int = stopDestinations.size

    fun setDestinations(newStopDestinations: List<StopDestination>, stopType: StopType, stopId: String) {
        this.stopType = stopType
        this.stopId = stopId
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = stopDestinations.size

            override fun getNewListSize() = newStopDestinations.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return (stopDestinations[oldItemPosition].line == newStopDestinations[newItemPosition].line
                        && stopDestinations[oldItemPosition].destination == newStopDestinations[newItemPosition].destination)
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return stopDestinations[oldItemPosition].times == newStopDestinations[newItemPosition].times
            }
        })
        stopDestinations = newStopDestinations
        result.dispatchUpdatesTo(this)
    }
}