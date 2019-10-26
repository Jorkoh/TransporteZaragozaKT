package com.jorkoh.transportezaragozakt.destinations.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.input.input
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.destinations.FragmentWithToolbar
import com.jorkoh.transportezaragozakt.destinations.materialColors
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.destinations.toColorFromHex
import com.jorkoh.transportezaragozakt.destinations.toHexFromColor
import kotlinx.android.synthetic.main.favorites_destination.*
import kotlinx.android.synthetic.main.favorites_destination.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : FragmentWithToolbar() {

    private val favoritesVM: FavoritesViewModel by viewModel()

    private val itemTouchHelper by lazy {
        ItemTouchHelper(ItemGestureHelper(object : ItemGestureHelper.OnItemGestureListener {
            override fun onItemDrag(fromPosition: Int, toPosition: Int): Boolean {
                // Dragging ongoing, modify the position visually to keep it snappy but don't bother persisting the change yet
                favorites_recycler_view.adapter?.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onItemDragged(fromPosition: Int, toPosition: Int) {
                // Dragging complete, persist the change
                favoritesVM.moveFavorite(fromPosition, toPosition)
            }

            override fun onItemSwiped(position: Int) {
                delete(favoriteStopsAdapter.favorites[position], position)
            }
        }))
    }

    private val openStop: (StopDetailsFragmentArgs) -> Unit = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            findNavController().navigate(
                FavoritesFragmentDirections.actionFavoritesToStopDetails(
                    info.stopType,
                    info.stopId
                )
            )
        }
    }

    private val editAlias: (FavoriteStopExtended) -> Unit = { favorite ->
        MaterialDialog(requireContext())
            .show {
                title(R.string.edit_favorite_dialog_title)
                input(prefill = favorite.alias) { _, newAlias ->
                    favoritesVM.updateFavorite(favorite.stopId, newAlias.toString(), favorite.colorHex)
                }
                positiveButton(R.string.edit)
            }
    }

    private val editColor: (FavoriteStopExtended) -> Unit = { favorite ->
        MaterialDialog(requireContext()).show {
            title(R.string.edit_favorite_dialog_title)
            colorChooser(materialColors, initialSelection = favorite.colorHex.toColorFromHex()) { _, color ->
                favoritesVM.updateFavorite(favorite.stopId, favorite.alias, color.toHexFromColor())
            }
            positiveButton(R.string.edit)
        }
    }

    private val restore: (FavoriteStopExtended) -> Unit = { favorite ->
        MaterialDialog(requireContext()).show {
            title(R.string.restore_favorite_title)
            message(R.string.restore_favorite_message)
            positiveButton(R.string.restore) {
                favoritesVM.restoreFavorite(favorite)
            }
            negativeButton(R.string.cancel)
        }
    }

    private val reorder: (RecyclerView.ViewHolder) -> Unit = { viewHolder ->
        itemTouchHelper.startDrag(viewHolder)
    }

    private val delete: (FavoriteStopExtended, Int) -> Unit = { favorite, position ->
        MaterialDialog(requireContext()).show {
            title(R.string.delete_favorite_title)
            message(R.string.delete_favorite_message)
            positiveButton(R.string.delete) {
                favoritesVM.deleteFavorite(favorite.stopId)
            }
            negativeButton(R.string.cancel) {
                favoriteStopsAdapter.notifyItemChanged(position)
            }
        }.onCancel {
            favoriteStopsAdapter.notifyItemChanged(position)
        }
    }

    private val favoriteStopsAdapter = FavoriteAdapter(openStop, editAlias, editColor, restore, reorder, delete)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favoritesVM.favoriteStops.observe(viewLifecycleOwner, Observer { favorites ->
            updateEmptyViewVisibility(favorites.isEmpty())
            favoriteStopsAdapter.setNewFavoriteStops(favorites)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.favorites_destination, container, false).apply {
            favorites_recycler_view.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = favoriteStopsAdapter
            }
            itemTouchHelper.attachToRecyclerView(favorites_recycler_view)
        }
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean) {
        val newVisibility = if (isEmpty) {
            View.VISIBLE
        } else {
            View.GONE
        }
        no_favorites_animation?.visibility = newVisibility
        no_favorites_text?.visibility = newVisibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid leaks
        favorites_recycler_view?.adapter = null
        itemTouchHelper.attachToRecyclerView(null)
    }
}
