package com.jorkoh.transportezaragozakt.destinations.stop_details

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
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsFragmentArgs
import kotlinx.android.synthetic.main.destination_row.view.*

class StopDestinationsAdapter(private val openLine: (LineDetailsFragmentArgs) -> Unit) : RecyclerView.Adapter<StopDestinationsAdapter.StopDestinationsViewHolder>() {

    class StopDestinationsViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    private var stopDestinations = listOf<StopDestination>()

    lateinit var stopType: StopType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopDestinationsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.destination_row, parent, false) as View
        return StopDestinationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopDestinationsViewHolder, position: Int) {
        holder.view.apply {
            line_text.text = stopDestinations[position].line
            line_text.setBackgroundColor(
                ContextCompat.getColor(
                    context, when (stopType) {
                        StopType.BUS -> R.color.bus_color
                        StopType.TRAM -> R.color.tram_color
                    }
                )
            )
            destination_text.text = stopDestinations[position].destination
            first_time_text.text = stopDestinations[position].times[0]
            second_time_text.text = stopDestinations[position].times[1]
            setOnClickListener(DebounceClickListener {
                openLine(LineDetailsFragmentArgs(stopType.toLineType().name, stopDestinations[position].line))
            })
        }
    }

    override fun getItemCount(): Int = stopDestinations.size

    fun setDestinations(newStopDestinations: List<StopDestination>, stopType: StopType) {
        this.stopType = stopType
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