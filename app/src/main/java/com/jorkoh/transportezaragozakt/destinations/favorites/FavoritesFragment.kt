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
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.jaredrummler.android.colorpicker.ColorShape
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.db.TagInfo
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragment
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_favorites.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class FavoritesFragment : Fragment(), ColorPickerDialogListener {

    private val favoritesVM: FavoritesViewModel by viewModel()

    private val itemOnClick: (TagInfo) -> Unit = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            val bundle = Bundle().apply {
                putString(StopDetailsFragment.STOP_ID_KEY, info.id)
                putString(StopDetailsFragment.STOP_TYPE_KEY, info.type.name)
            }
            findNavController().navigate(R.id.action_favorites_to_stopDetails, bundle)
        }
    }

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = ItemGestureHelper(object : ItemGestureHelper.OnItemGestureListener {
            override fun onItemDrag(fromPosition: Int, toPosition: Int): Boolean {
                recycler_view.adapter?.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onItemDragged(fromPosition: Int, toPosition: Int) {
                favoritesVM.moveFavorite(fromPosition, toPosition)
            }

            override fun onItemSwiped(position: Int) {}
        })
        ItemTouchHelper(simpleItemTouchCallback)
    }

    private val onEditClick: (FavoriteStopExtended) -> Unit = { favorite ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            val editFavoriteDialog = ColorPickerDialog.newBuilder().apply {
                setShowColorShades(false)
                setShowAlphaSlider(false)
                setAllowCustom(true)
                setColorShape(ColorShape.CIRCLE)
                setDialogId(favorite.stopId)
                setDialogType(ColorPickerDialog.TYPE_PRESETS)
                setDialogTitle(R.string.edit_favorite_dialog_title)
                setAlias(favorite.alias)
                setCustomButtonText(R.string.edit_favorite_dialog_restore)
                if (favorite.colorHex.isNotEmpty()) {
                    setColor(Color.parseColor(favorite.colorHex))
                }
            }.create()
            editFavoriteDialog.setColorPickerDialogListener(this)
            editFavoriteDialog.show(requireFragmentManager(), "EditFavoriteDialog")
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
        FavoriteStopsAdapter(itemOnClick, onEditClick, onReorderClick)

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

        val rootView = inflater.inflate(R.layout.fragment_favorites, container, false)

        rootView.recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = favoriteStopsAdapter
//            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        itemTouchHelper.attachToRecyclerView(rootView.recycler_view)

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
