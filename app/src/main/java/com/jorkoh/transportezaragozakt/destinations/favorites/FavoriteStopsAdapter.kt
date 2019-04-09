package com.jorkoh.transportezaragozakt.destinations.favorites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.*
import kotlinx.android.synthetic.main.stop_row.view.*

class FavoriteStopsAdapter(
    private val clickListener: (TagInfo) -> Unit,
    private val longClickListener: (FavoriteStopExtended) -> Boolean
) : RecyclerView.Adapter<FavoriteStopsAdapter.StopDetailsViewHolder>() {

    class StopDetailsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            favorite: FavoriteStopExtended,
            clickListener: (TagInfo) -> Unit,
            longClickListener: (FavoriteStopExtended) -> Boolean
        ) {
            itemView.apply {
                type_image.setImageResource(
                    when (favorite.type) {
                        StopType.BUS -> R.drawable.ic_bus
                        StopType.TRAM -> R.drawable.ic_tram
                    }
                )
                title_text.text = favorite.alias
                setOnClickListener { clickListener(TagInfo(favorite.stopId, favorite.type)) }
                setOnLongClickListener { longClickListener(favorite) }
            }
        }
    }

    lateinit var favorites: List<FavoriteStopExtended>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stop_row, parent, false) as View
        return StopDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopDetailsViewHolder, position: Int) {
        holder.bind(favorites[position], clickListener, longClickListener)
    }

    override fun getItemCount(): Int = if (::favorites.isInitialized) favorites.size else 0

    fun setFavoriteStops(newFavorites: List<FavoriteStopExtended>) {
        if (::favorites.isInitialized) {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = favorites.size

                override fun getNewListSize() = newFavorites.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return favorites[oldItemPosition].stopId == newFavorites[newItemPosition].stopId
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return favorites[oldItemPosition].type == newFavorites[newItemPosition].type
                            && favorites[oldItemPosition].alias == newFavorites[newItemPosition].alias
                            && favorites[oldItemPosition].colorHex == newFavorites[newItemPosition].colorHex
                }
            })
            favorites = newFavorites
            result.dispatchUpdatesTo(this)
        } else {
            favorites = newFavorites
            notifyItemRangeInserted(0, favorites.size)
        }
    }
}