package com.jorkoh.transportezaragozakt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import kotlinx.android.synthetic.main.stop_row.view.*

class FavoriteStopsAdapter: RecyclerView.Adapter<FavoriteStopsAdapter.StopDetailsViewHolder>() {

    class StopDetailsViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    lateinit var stops: List<Stop>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stop_row, parent, false) as View
        return StopDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopDetailsViewHolder, position: Int) {
        holder.view.apply{
            type_text.text = stops[position].type.name
            title_text.text = stops[position].title
        }
    }

    override fun getItemCount(): Int = if (::stops.isInitialized) stops.size else 0

    fun setFavoriteStops(newStops : List<Stop>){
        if (::stops.isInitialized) {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = stops.size

                override fun getNewListSize() = newStops.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = stops === newStops

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = stops == newStops
            })
            stops = newStops
            result.dispatchUpdatesTo(this)
        }else{
            stops = newStops
            notifyItemRangeInserted(0, stops.size)
        }
    }
}