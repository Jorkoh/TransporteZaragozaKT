package com.jorkoh.transportezaragozakt.destinations.favorites

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import kotlinx.android.synthetic.main.favorite_row.view.*

class FavoriteAdapter(
    private val openStop: (StopDetailsFragmentArgs) -> Unit,
    private val editAlias: (FavoriteStopExtended) -> Unit,
    private val editColor: (FavoriteStopExtended) -> Unit,
    private val restore: (FavoriteStopExtended) -> Unit,
    private val reorder: (RecyclerView.ViewHolder) -> Unit,
    private val delete: (FavoriteStopExtended, Int) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            favorite: FavoriteStopExtended,
            openStop: (StopDetailsFragmentArgs) -> Unit,
            editAlias: (FavoriteStopExtended) -> Unit,
            editColor: (FavoriteStopExtended) -> Unit,
            restore: (FavoriteStopExtended) -> Unit,
            reorder: (RecyclerView.ViewHolder) -> Unit,
            delete: (FavoriteStopExtended, Int) -> Unit
        ) {
            itemView.apply {
                type_image_favorite.setImageResource(
                    when (favorite.type) {
                        StopType.BUS -> R.drawable.ic_bus
                        StopType.TRAM -> R.drawable.ic_tram
                    }
                )
                title_text_favorite.text = favorite.alias
                number_text_favorite.text = favorite.number
                if (favorite.colorHex.isNotEmpty()) {
                    favorite_color.setBackgroundColor(Color.parseColor(favorite.colorHex))
                    favorite_color.visibility = View.VISIBLE
                } else {
                    favorite_color.setBackgroundColor(Color.TRANSPARENT)
                    favorite_color.visibility = View.GONE
                }

                itemView.lines_layout_favorite.removeAllViews()
                val layoutInflater = LayoutInflater.from(context)
                favorite.lines.forEachIndexed { index, line ->
                    layoutInflater.inflate(R.layout.map_info_window_line, itemView.lines_layout_favorite)
                    val lineView = itemView.lines_layout_favorite.getChildAt(index) as TextView

                    val lineColor = if (favorite.type == StopType.BUS) R.color.bus_color else R.color.tram_color
                    lineView.background.setColorFilter(
                        ContextCompat.getColor(context, lineColor),
                        PorterDuff.Mode.SRC_IN
                    )
                    lineView.text = line
                }

                setOnClickListener(DebounceClickListener {
                    openStop(StopDetailsFragmentArgs(favorite.type.name, favorite.stopId))
                })
                edit_view_favorite.setOnClickListener {
                    PopupMenu(context, it).apply {
                        menu.apply {
                            add(context.resources.getString(R.string.alias)).setOnMenuItemClickListener {
                                editAlias(favorite)
                                true
                            }
                            add(context.resources.getString(R.string.color)).setOnMenuItemClickListener {
                                editColor(favorite)
                                true
                            }
                            add(context.resources.getString(R.string.restore)).setOnMenuItemClickListener {
                                restore(favorite)
                                true
                            }
                            add(context.resources.getString(R.string.delete)).setOnMenuItemClickListener {
                                delete(favorite, adapterPosition)
                                true
                            }
                        }
                        show()
                    }
                }
                reorder_view_favorite.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        reorder(this@FavoriteViewHolder)
                    }
                    return@setOnTouchListener true
                }
            }
        }
    }

    lateinit var favorites: List<FavoriteStopExtended>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorite_row, parent, false) as View
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favorites[position], openStop, editAlias, editColor, restore, reorder, delete)
    }

    override fun getItemCount(): Int = if (::favorites.isInitialized) favorites.size else 0

    fun setNewFavoriteStops(newFavorites: List<FavoriteStopExtended>) {
        if (::favorites.isInitialized) {
            if (isOnlyPositionChange(newFavorites)) {
                favorites = newFavorites
            } else {
                val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize() = favorites.size

                    override fun getNewListSize() = newFavorites.size

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        return favorites[oldItemPosition].stopId == newFavorites[newItemPosition].stopId
                    }

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        return favorites[oldItemPosition].type == newFavorites[newItemPosition].type
                                && favorites[oldItemPosition].lines == newFavorites[newItemPosition].lines
                                && favorites[oldItemPosition].alias == newFavorites[newItemPosition].alias
                                && favorites[oldItemPosition].colorHex == newFavorites[newItemPosition].colorHex
                    }
                })
                favorites = newFavorites
                result.dispatchUpdatesTo(this)
            }
        } else {
            favorites = newFavorites
            notifyItemRangeInserted(0, favorites.size)
        }
    }

    private fun isOnlyPositionChange(newFavorites: List<FavoriteStopExtended>) =
        newFavorites.count() == favorites.count() && newFavorites.containsAll(favorites)
}