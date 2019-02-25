package com.jorkoh.transportezaragozakt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import kotlinx.android.synthetic.main.row_view.view.*

class StopDetailsAdapter: RecyclerView.Adapter<StopDetailsAdapter.StopDetailsViewHolder>() {

    class StopDetailsViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    lateinit var stopDestinations: List<StopDestination>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_view, parent, false) as View
        return StopDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopDetailsViewHolder, position: Int) {
        holder.view.apply{
            line_text.text = stopDestinations[position].line
            destination_text.text = stopDestinations[position].destination
            first_time_text.text = stopDestinations[position].times[0].toString()
            second_time_text.text = stopDestinations[position].times[1].toString()
        }
    }

    override fun getItemCount(): Int = if (::stopDestinations.isInitialized) stopDestinations.size else 0

    fun setDestinations(newStopDestinations : List<StopDestination>){
        if (::stopDestinations.isInitialized) {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = stopDestinations.size

                override fun getNewListSize() = newStopDestinations.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = stopDestinations === newStopDestinations

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = stopDestinations == newStopDestinations
            })
            stopDestinations = newStopDestinations
            result.dispatchUpdatesTo(this)
        }else{
            stopDestinations = newStopDestinations
            notifyItemRangeInserted(0, stopDestinations.size)
        }
    }
}