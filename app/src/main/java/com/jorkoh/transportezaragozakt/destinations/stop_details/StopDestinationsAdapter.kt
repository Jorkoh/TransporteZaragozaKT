package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import kotlinx.android.synthetic.main.destination_row.view.*

class StopDestinationsAdapter: RecyclerView.Adapter<StopDestinationsAdapter.StopDestintaionsViewHolder>() {

    class StopDestintaionsViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    lateinit var stopDestinations: List<StopDestination>

    lateinit var stopType: StopType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopDestintaionsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.destination_row, parent, false) as View
        return StopDestintaionsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopDestintaionsViewHolder, position: Int) {
        holder.view.apply {
            line_text.text = stopDestinations[position].line
            line_text.setBackgroundResource(when(stopType){
                StopType.BUS -> R.drawable.ic_frame_bus_stop
                StopType.TRAM -> R.drawable.ic_frame_tram_stop
            })
            destination_text.text = stopDestinations[position].destination
            first_time_text.text = stopDestinations[position].times[0]
            second_time_text.text = stopDestinations[position].times[1]
        }
    }

    override fun getItemCount(): Int = if (::stopDestinations.isInitialized) stopDestinations.size else 0

    fun setDestinations(newStopDestinations: List<StopDestination>, stopType: StopType) {
        this.stopType = stopType
        if (::stopDestinations.isInitialized) {
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
        } else {
            stopDestinations = newStopDestinations
            notifyItemRangeInserted(0, stopDestinations.size)
        }
    }
}