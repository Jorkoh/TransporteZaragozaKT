package com.jorkoh.transportezaragozakt.destinations.favorites

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.input.input
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.db.TagInfo
import kotlinx.android.synthetic.main.favorites_destination.*
import kotlinx.android.synthetic.main.favorites_destination.view.*
import kotlinx.android.synthetic.main.main_container.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class FavoritesFragment : Fragment() {

    private val favoritesVM: FavoritesViewModel by viewModel()

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = ItemGestureHelper(object : ItemGestureHelper.OnItemGestureListener {
            override fun onItemDrag(fromPosition: Int, toPosition: Int): Boolean {
                favorites_recycler_view.adapter?.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onItemDragged(fromPosition: Int, toPosition: Int) {
                favoritesVM.moveFavorite(fromPosition, toPosition)
            }

            override fun onItemSwiped(position: Int) {
                delete(favoriteStopsAdapter.favorites[position], position)
            }
        })
        ItemTouchHelper(simpleItemTouchCallback)
    }

    private val openStop: (TagInfo) -> Unit = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            findNavController().navigate(
                FavoritesFragmentDirections.actionFavoritesToStopDetails(
                    info.type.name,
                    info.id
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
                positiveButton(R.string.edit_button)
            }
    }

    private val editColor: (FavoriteStopExtended) -> Unit = { favorite ->
        MaterialDialog(requireContext()).show {
            title(R.string.edit_favorite_dialog_title)
            colorChooser(
                materialColors,
                initialSelection = if (favorite.colorHex.isNullOrEmpty()) Color.TRANSPARENT else Color.parseColor(
                    favorite.colorHex
                )
            ) { _, color ->
                val hexColor = if (color == Color.TRANSPARENT) "" else String.format("#%06X", 0xFFFFFF and color)
                favoritesVM.updateFavorite(favorite.stopId, favorite.alias, hexColor)
            }
            positiveButton(R.string.edit_button)
        }
    }

    private val restore: (FavoriteStopExtended) -> Unit = { favorite ->
        MaterialDialog(requireContext()).show {
            title(R.string.restore_favorite_title)
            message(R.string.restore_favorite_message)
            positiveButton(R.string.restore) {
                favoritesVM.restoreFavorite(favorite.stopId)
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
        }
    }

    private val favoriteStopsAdapter = FavoriteAdapter(openStop, editAlias, editColor, restore, reorder, delete)

    private val favoriteStopsObserver = Observer<List<FavoriteStopExtended>> { favorites ->
        favorites?.let {
            updateEmptyViewVisibility(favorites.isEmpty(), view)
            favoriteStopsAdapter.setNewFavoriteStops(favorites)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.favorites_destination, container, false)

        rootView.favorites_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = favoriteStopsAdapter
        }

        itemTouchHelper.attachToRecyclerView(rootView.favorites_recycler_view)

        favoritesVM.favoriteStops.observe(this, favoriteStopsObserver)
        updateEmptyViewVisibility(favoritesVM.favoriteStops.value.isNullOrEmpty(), rootView)
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favoritesVM.init()
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean, rootView: View?) {
        val newVisibility = if (isEmpty) {
            View.VISIBLE
        } else {
            View.GONE
        }
        rootView?.no_favorites_animation?.visibility = newVisibility
        rootView?.no_favorites_text?.visibility = newVisibility
    }
}
