package com.jorkoh.transportezaragozakt.destinations.favorites

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.*
import kotlinx.android.synthetic.main.favorite_row.view.*

class FavoriteStopsAdapter(
    private val openStop: (TagInfo) -> Unit,
    private val editAlias: (FavoriteStopExtended) -> Unit,
    private val editColor: (FavoriteStopExtended) -> Unit,
    private val restore: (FavoriteStopExtended) -> Unit,
    private val reorder: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<FavoriteStopsAdapter.StopDetailsViewHolder>() {

    class StopDetailsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            favorite: FavoriteStopExtended,
            openStop: (TagInfo) -> Unit,
            editAlias: (FavoriteStopExtended) -> Unit,
            editColor: (FavoriteStopExtended) -> Unit,
            restore: (FavoriteStopExtended) -> Unit,
            reorder: (RecyclerView.ViewHolder) -> Unit
        ) {
            itemView.apply {
                type_image_favorite.setImageResource(
                    when (favorite.type) {
                        StopType.BUS -> R.drawable.ic_bus
                        StopType.TRAM -> R.drawable.ic_tram
                    }
                )
                title_text_favorite.text = favorite.alias
                if (favorite.colorHex.isNotEmpty()) {
                    favorite_color.setBackgroundColor(Color.parseColor(favorite.colorHex))
                    favorite_color.visibility = View.VISIBLE
                } else {
                    favorite_color.setBackgroundColor(Color.TRANSPARENT)
                    favorite_color.visibility = View.GONE
                }

                itemView.lines_layout.removeAllViews()
                val layoutInflater = LayoutInflater.from(context)
                favorite.lines.forEachIndexed { index, line ->
                    layoutInflater.inflate(R.layout.map_info_window_line, itemView.lines_layout)
                    val lineView = itemView.lines_layout.getChildAt(index) as TextView

                    val lineColor = if (favorite.type == StopType.BUS) R.color.bus_color else R.color.tram_color
                    lineView.background.setColorFilter(
                        ContextCompat.getColor(context, lineColor),
                        PorterDuff.Mode.SRC_IN
                    )
                    lineView.text = line
                }

                setOnClickListener { openStop(TagInfo(favorite.stopId, favorite.type)) }
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
                        }
                        show()
                    }
                }
                reorder_view_favorite.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        reorder(this@StopDetailsViewHolder)
                    }
                    return@setOnTouchListener true
                }
            }
        }
    }

    lateinit var favorites: List<FavoriteStopExtended>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorite_row, parent, false) as View
        return StopDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopDetailsViewHolder, position: Int) {
        holder.bind(favorites[position], openStop, editAlias, editColor, restore, reorder)
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