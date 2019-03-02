package com.jorkoh.transportezaragozakt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.db.TagInfo
import kotlinx.android.synthetic.main.stop_row.view.*

class FavoriteStopsAdapter(
    private val clickListener: (TagInfo) -> Unit,
    private val longClickListener: (TagInfo) -> Boolean
) : RecyclerView.Adapter<FavoriteStopsAdapter.StopDetailsViewHolder>() {

    class StopDetailsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(stop: Stop, clickListener: (TagInfo) -> Unit, longClickListener: (TagInfo) -> Boolean) {
            itemView.apply {
                type_image.setImageResource(
                    when (stop.type) {
                        StopType.BUS -> R.drawable.ic_bus
                        StopType.TRAM -> R.drawable.ic_tram
                    }
                )
                title_text.text = stop.title
                setOnClickListener { clickListener(TagInfo(stop.id, stop.type)) }
                setOnLongClickListener { longClickListener(TagInfo(stop.id, stop.type)) }
            }
        }
    }

    lateinit var stops: List<Stop>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stop_row, parent, false) as View
        return StopDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopDetailsViewHolder, position: Int) {
        holder.bind(stops[position], clickListener, longClickListener)
    }

    override fun getItemCount(): Int = if (::stops.isInitialized) stops.size else 0

    fun setFavoriteStops(newStops: List<Stop>) {
        if (::stops.isInitialized) {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = stops.size

                override fun getNewListSize() = newStops.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = stops === newStops

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = stops == newStops
            })
            stops = newStops
            result.dispatchUpdatesTo(this)
        } else {
            stops = newStops
            notifyItemRangeInserted(0, stops.size)
        }
    }
}