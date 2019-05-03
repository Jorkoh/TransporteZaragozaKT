package com.jorkoh.transportezaragozakt.destinations.favorites

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.db.TagInfo
import kotlinx.android.synthetic.main.favorites_destination.*
import kotlinx.android.synthetic.main.favorites_destination.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class FavoritesFragment : Fragment(), ColorPickerDialogListener {

    private val favoritesVM: FavoritesViewModel by viewModel()

    private val itemOnClick: (TagInfo) -> Unit = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            findNavController().navigate(
                FavoritesFragmentDirections.actionFavoritesToStopDetails(
                    info.type.name,
                    info.id
                )
            )
        }
    }

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = ItemGestureHelper(object : ItemGestureHelper.OnItemGestureListener {
            override fun onItemDrag(fromPosition: Int, toPosition: Int): Boolean {
                favorites_recycler_view.adapter?.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onItemDragged(fromPosition: Int, toPosition: Int) {
                favoritesVM.moveFavorite(fromPosition, toPosition)
            }

            override fun onItemSwiped(position: Int) {}
        })
        ItemTouchHelper(simpleItemTouchCallback)
    }

    private val onEditAlias: (FavoriteStopExtended) -> Unit = { favorite ->
        MaterialDialog(requireContext())
            .show {
                title(R.string.create_shortcut)
                input(prefill = favorite.alias) { _, newAlias ->
                    favoritesVM.updateFavorite(newAlias.toString(), favorite.colorHex, favorite.stopId)
                }
                positiveButton(R.string.edit_button)
            }
    }


    private val onEditColor: (FavoriteStopExtended) -> Unit = { favorite ->
        val colors = intArrayOf(
            Color.TRANSPARENT,
            -0xbbcca,
            -0x16e19d,
            -0xd36d,
            -0x63d850,
            -0x98c549,
            -0xc0ae4b,
            -0xde690d,
            -0xfc560c,
            -0xff432c,
            -0xff6978,
            -0xb350b0,
            -0x743cb6,
            -0x3223c7,
            -0x14c5,
            -0x3ef9,
            -0x6800,
            -0x86aab8,
            -0x9f8275,
            Color.BLACK
        )
        MaterialDialog(requireContext()).show {
            title(R.string.edit_favorite_dialog_title)
            colorChooser(colors) { _, color ->
                val hexColor = if (color == Color.TRANSPARENT) "" else String.format("#%06X", 0xFFFFFF and color)
                favoritesVM.updateFavorite(favorite.alias, hexColor, favorite.stopId)
            }
            positiveButton(R.string.edit_button)
        }
    }

    private val onReorderClick: (RecyclerView.ViewHolder) -> Unit = { viewHolder ->
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onDialogDismissed(dialogId: String) {}

    override fun onDialogAccepted(favoriteId: String, alias: String, color: Int) {
        val hexColor = if (color == Color.TRANSPARENT) "" else String.format("#%06X", 0xFFFFFF and color)
        favoritesVM.updateFavorite(alias, hexColor, favoriteId)
    }

    override fun onDialogRestore(dialogId: String) {
        favoritesVM.restoreFavorite(dialogId)
    }

    private val favoriteStopsAdapter: FavoriteStopsAdapter =
        FavoriteStopsAdapter(itemOnClick, onEditAlias, onEditColor, onReorderClick)

    private val favoriteStopsObserver = Observer<List<FavoriteStopExtended>> { favorites ->
        favorites?.let {
            Log.d("TESTING STUFF", "setting favorites")
            updateEmptyViewVisibility(favorites.isEmpty(), view)
            favoriteStopsAdapter.setFavoriteStops(favorites)
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

        favoritesVM.getFavoriteStops().observe(this, favoriteStopsObserver)
        updateEmptyViewVisibility(favoritesVM.getFavoriteStops().value.isNullOrEmpty(), rootView)
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
