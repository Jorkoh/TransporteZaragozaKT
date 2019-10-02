package com.jorkoh.transportezaragozakt.destinations.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.inflateLines
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.tracking_row.*

class TrackingsAdapter(
    private val selectTracking: (RuralTracking) -> Unit
) : RecyclerView.Adapter<TrackingsAdapter.TrackingViewHolder>() {

    class TrackingViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        val context : Context
            get() = itemView.context

        fun bind(
            tracking: RuralTracking,
            selectTracking: (RuralTracking) -> Unit
        ) {
            // Texts
            title_text_tracking.text = tracking.lineName
            number_text_tracking.text = tracking.vehicleId
            number_text_tracking.contentDescription = context.getString(R.string.vehicle_template, tracking.vehicleId)
            // Lines
            listOf(tracking.lineId).inflateLines(lines_layout_tracking, StopType.RURAL, context)
            // Listeners
            itemView.setOnClickListener(DebounceClickListener {
                selectTracking(tracking)
            })
        }
    }

    private var trackings: List<RuralTracking> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tracking_row, parent, false)
        return TrackingViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackingViewHolder, position: Int) {
        holder.bind(trackings[position], selectTracking)
    }

    override fun getItemCount(): Int = trackings.size

    fun setNewTrackings(newTrackings: List<RuralTracking>) {
        val oldSize = trackings.size
        trackings = newTrackings.sortedBy { it.lineId + it.lineName }
        notifyItemRangeRemoved(0, oldSize)
    }
}