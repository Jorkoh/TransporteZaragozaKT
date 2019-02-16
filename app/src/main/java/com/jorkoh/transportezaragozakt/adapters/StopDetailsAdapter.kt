package com.jorkoh.transportezaragozakt.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.models.IStop
import com.jorkoh.transportezaragozakt.R

class StopDetailsAdapter: RecyclerView.Adapter<StopDetailsAdapter.StopDetailsViewHolder>() {

    class StopDetailsViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    lateinit var stopInfo: IStop

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopDetailsViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.row_view, parent, false) as TextView
        return StopDetailsViewHolder(textView)
    }

    override fun onBindViewHolder(holder: StopDetailsViewHolder, position: Int) {
        holder.textView.text = stopInfo.destinations[position].line
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