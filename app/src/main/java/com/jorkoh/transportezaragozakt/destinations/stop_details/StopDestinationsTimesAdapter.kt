package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.db.toLineType
import com.jorkoh.transportezaragozakt.destinations.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.fixTimes
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.destinations.toPx
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.destination_row.*

class StopDestinationsTimesAdapter(
    private val openLine: (LineDetailsFragmentArgs) -> Unit,
    private val openNotTrackedWarning: () -> Unit
) : RecyclerView.Adapter<StopDestinationsTimesAdapter.StopDestinationsViewHolder>() {

    class StopDestinationsViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        val context: Context
            get() = itemView.context

        fun bind(
            destination: StopDestination,
            stopType: StopType,
            stopId: String,
            openLine: (LineDetailsFragmentArgs) -> Unit,
            openNotTrackedWarning: () -> Unit
        ) {
            // Line number and color
            line_text.text = destination.line
            line_text.contentDescription = context.getString(R.string.line_template, destination.line)
            line_text.setBackgroundColor(
                ContextCompat.getColor(
                    context, when (stopType) {
                        StopType.BUS -> R.color.bus_color
                        StopType.TRAM -> R.color.tram_color
                        StopType.RURAL -> R.color.rural_color
                    }
                )
            )
            line_text.minWidth = when (stopType) {
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
                openLine(LineDetailsFragmentArgs(stopType.toLineType().name, destination.line, stopId))
            })
            first_time_warning.setOnClickListener{
                openNotTrackedWarning()
            }
            second_time_warning.setOnClickListener{
                openNotTrackedWarning()
            }
        }
    }

    private var stopDestinations = listOf<StopDestination>()

    lateinit var stopType: StopType
    lateinit var stopId: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopDestinationsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.destination_row, parent, false) as View
        return StopDestinationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopDestinationsViewHolder, position: Int) {
        holder.bind(stopDestinations[position], stopType, stopId, openLine, openNotTrackedWarning)
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