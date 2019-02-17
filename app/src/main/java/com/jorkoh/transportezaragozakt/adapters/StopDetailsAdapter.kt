package com.jorkoh.transportezaragozakt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.models.IStop
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.row_view.view.*

class StopDetailsAdapter: RecyclerView.Adapter<StopDetailsAdapter.StopDetailsViewHolder>() {

    class StopDetailsViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    lateinit var stopInfo: IStop

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_view, parent, false) as View
        return StopDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopDetailsViewHolder, position: Int) {
        holder.view.apply{
            line_text.text = stopInfo.destinations[position].line
            destination_text.text = stopInfo.destinations[position].destination
            first_time_text.text = stopInfo.destinations[position].times[0].toString()
            second_time_text.text = stopInfo.destinations[position].times[1].toString()
        }
    }

    override fun getItemCount(): Int = if (::stopInfo.isInitialized) stopInfo.destinations.size else 0

    fun setDestinations(newStopInfo : IStop){
        if (::stopInfo.isInitialized) {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = stopInfo.destinations.size

                override fun getNewListSize() = newStopInfo.destinations.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = stopInfo === newStopInfo

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = stopInfo == newStopInfo
            })
            stopInfo = newStopInfo
            result.dispatchUpdatesTo(this)
        }else{
            stopInfo = newStopInfo
            notifyItemRangeInserted(0, stopInfo.destinations.size)
        }
    }
}