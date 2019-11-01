package com.jorkoh.transportezaragozakt.destinations.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.inflateLines
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.stop_row.*

class TrackingsAdapter(
    private val selectTracking: (RuralTracking) -> Unit
) : RecyclerView.Adapter<TrackingsAdapter.TrackingViewHolder>() {

    class TrackingViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        val context : Context
            get() = itemView.context

        @SuppressLint("SetTextI18n")
        fun bind(
            tracking: RuralTracking,
            selectTracking: (RuralTracking) -> Unit,
            userLocation: Location?
        ) {
            // Icon
            type_image_stop.setImageResource(R.drawable.ic_rural_tracking)
            type_image_stop.contentDescription = context.getString(R.string.rural_tracking)
            // Texts
            title_stop.text = tracking.lineName
            number_stop.text = tracking.vehicleId
            number_stop.contentDescription = context.getString(R.string.vehicle_template, tracking.vehicleId)
            // Lines
            listOf(tracking.lineId).inflateLines(lines_layout_stop, StopType.RURAL, context)
            // Favorite icon
            favorite_icon_stop.visibility = View.GONE
            // Listeners
            itemView.setOnClickListener(DebounceClickListener {
                selectTracking(tracking)
            })
            if(userLocation != null){
                val distance = FloatArray(1)
                Location.distanceBetween(
                    userLocation.latitude,
                    userLocation.longitude,
                    tracking.location.latitude,
                    tracking.location.longitude,
                    distance
                )
                distance_stop.text = "${"%.0f".format(distance[0])} m."
            }
        }
    }

    private var trackings: List<RuralTracking> = listOf()
    private var userLocation : Location? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stop_row, parent, false)
        return TrackingViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackingViewHolder, position: Int) {
        holder.bind(trackings[position], selectTracking, userLocation)
    }

    override fun getItemCount(): Int = trackings.size

    fun setNewTrackings(newTrackings: List<RuralTracking>) {
        val sortedNewTrackings = newTrackings.sortedBy { it.lineId + it.lineName + it.vehicleId}
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = trackings.size

            override fun getNewListSize() = sortedNewTrackings.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return trackings[oldItemPosition].vehicleId == sortedNewTrackings[newItemPosition].vehicleId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return trackings[oldItemPosition].lineId == sortedNewTrackings[newItemPosition].lineId
                        && trackings[oldItemPosition].lineName == sortedNewTrackings[newItemPosition].lineName
            }
        })
        trackings = sortedNewTrackings
        result.dispatchUpdatesTo(this)
    }

    fun setNewLocation(newLocation: Location){
        userLocation = newLocation
        // Check this
        notifyDataSetChanged()
    }
}