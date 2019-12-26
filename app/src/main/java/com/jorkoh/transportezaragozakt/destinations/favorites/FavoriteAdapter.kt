package com.jorkoh.transportezaragozakt.destinations.favorites

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragment
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.destinations.utils.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.utils.inflateLines
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.favorite_row.*

private const val STATE_LAST_SELECTED_ID = "last_selected_id"

class FavoriteAdapter(
    private val openStop: (StopDetailsFragmentArgs, Array<Pair<View, String>>) -> Unit,
    private val editAlias: (FavoriteStopExtended) -> Unit,
    private val editColor: (FavoriteStopExtended) -> Unit,
    private val restore: (FavoriteStopExtended) -> Unit,
    private val reorder: (RecyclerView.ViewHolder) -> Unit,
    private val delete: (FavoriteStopExtended, Int) -> Unit,
    private val onReadyToTransition: () -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        val context: Context
            get() = itemView.context

        fun bind(
            favorite: FavoriteStopExtended,
            openStop: (StopDetailsFragmentArgs, Array<Pair<View, String>>) -> Unit,
            editAlias: (FavoriteStopExtended) -> Unit,
            editColor: (FavoriteStopExtended) -> Unit,
            restore: (FavoriteStopExtended) -> Unit,
            reorder: (RecyclerView.ViewHolder) -> Unit,
            delete: (FavoriteStopExtended, Int) -> Unit
        ) {
            // Stop type icon
            when (favorite.type) {
                StopType.BUS -> {
                    favorite_row_type_image.setImageResource(R.drawable.ic_bus_stop)
                    favorite_row_type_image.contentDescription = context.getString(R.string.stop_type_bus)
                }
                StopType.TRAM -> {
                    favorite_row_type_image.setImageResource(R.drawable.ic_tram_stop)
                    favorite_row_type_image.contentDescription = context.getString(R.string.stop_type_tram)
                }
                StopType.RURAL -> {
                    favorite_row_type_image.setImageResource(R.drawable.ic_rural_stop)
                    favorite_row_type_image.contentDescription = context.getString(R.string.stop_type_rural)
                }
            }
            // Texts
            favorite_row_title_text.text = favorite.alias
            number_text_favorite.text = favorite.number
            number_text_favorite.contentDescription = context.getString(R.string.number_template, favorite.number)
            // Favorite user defined color
            if (favorite.colorHex.isNotEmpty()) {
                favorite_row_color.setBackgroundColor(Color.parseColor(favorite.colorHex))
                favorite_row_color.visibility = View.VISIBLE
            } else {
                favorite_row_color.setBackgroundColor(Color.TRANSPARENT)
                favorite_row_color.visibility = View.GONE
            }
            // Lines
            favorite.lines.inflateLines(favorite_row_lines_layout, favorite.type, context)
            // Listeners
            itemView.setOnClickListener(DebounceClickListener {
                // Record the selected item so that we can make the item ready before starting the
                // reenter transition.
                lastSelectedId = favorite.stopId

                openStop(
                    StopDetailsFragmentArgs(favorite.type.name, favorite.stopId),
                    arrayOf(
                        favorite_row_card to StopDetailsFragment.TRANSITION_NAME_BACKGROUND,
                        favorite_row_layout to StopDetailsFragment.TRANSITION_NAME_APPBAR,
                        favorite_row_toolbar to StopDetailsFragment.TRANSITION_NAME_TOOLBAR,
                        favorite_row_type_image to StopDetailsFragment.TRANSITION_NAME_IMAGE,
                        favorite_row_title_text to StopDetailsFragment.TRANSITION_NAME_TITLE,
                        favorite_row_lines_layout to StopDetailsFragment.TRANSITION_NAME_LINES
                    )
                )
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

    private var lastSelectedId: String? = null

    val expectsTransition: Boolean
        get() = lastSelectedId != null

    var favorites: List<FavoriteStopExtended> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorite_row, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favorite = favorites[position]

        ViewCompat.setTransitionName(holder.favorite_row_card, "favorite_row_card_${favorite.stopId}")
        ViewCompat.setTransitionName(holder.favorite_row_layout, "favorite_row_layout_${favorite.stopId}")
        ViewCompat.setTransitionName(holder.favorite_row_toolbar, "favorite_row_toolbar_${favorite.stopId}")
        ViewCompat.setTransitionName(holder.favorite_row_type_image, "favorite_row_type_image_${favorite.stopId}")
        ViewCompat.setTransitionName(holder.favorite_row_title_text, "favorite_row_title_text_${favorite.stopId}")
        ViewCompat.setTransitionName(holder.favorite_row_lines_layout, "favorite_row_lines_layout_${favorite.stopId}")

        holder.bind(favorite, openStop, editAlias, editColor, restore, reorder, delete)

        if (favorite.stopId == lastSelectedId) {
            onReadyToTransition()
            lastSelectedId = null
        }
    }

    fun saveInstanceState(outState: Bundle) {
        lastSelectedId?.let { id ->
            outState.putString(STATE_LAST_SELECTED_ID, id)
        }
    }

    fun restoreInstanceState(state: Bundle) {
        if (lastSelectedId == null && state.containsKey(STATE_LAST_SELECTED_ID)) {
            lastSelectedId = state.getString(STATE_LAST_SELECTED_ID)
        }
    }

    override fun getItemCount(): Int = favorites.size

    fun setNewFavoriteStops(newFavorites: List<FavoriteStopExtended>) {
        if (isOnlyPositionChange(newFavorites)) {
            // In the case of drag and drop positional changes the reordering has already taken place visually
            favorites = newFavorites
        } else {
            // In any other case the difference is calculated with DiffUtil
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
    }

    private fun isOnlyPositionChange(newFavorites: List<FavoriteStopExtended>) =
        newFavorites.count() == favorites.count() && newFavorites.containsAll(favorites)
}