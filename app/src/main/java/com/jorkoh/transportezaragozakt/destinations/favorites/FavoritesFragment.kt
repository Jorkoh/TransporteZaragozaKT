package com.jorkoh.transportezaragozakt.destinations.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.TagInfo
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragment
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_favorites.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(): FavoritesFragment =
            FavoritesFragment()
    }

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

    //TODO: TESTING
    private val itemOnLongClick: (TagInfo) -> Boolean = { _ ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Toast.makeText(context, "Long press", Toast.LENGTH_LONG).show()
        }
        true
    }

    private val favoriteStopsAdapter: FavoriteStopsAdapter =
        FavoriteStopsAdapter(itemOnClick, itemOnLongClick)

    private val favoriteStopsObserver = Observer<List<Stop>> { favorites ->
        favorites?.let {
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
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        favoritesVM.getFavoriteStops().observe(this, favoriteStopsObserver)
        updateEmptyViewVisibility(favoritesVM.getFavoriteStops().value.isNullOrEmpty(), rootView)
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favoritesVM.init()
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean, rootView : View?) {
        val newVisibility = if (isEmpty) {
            View.VISIBLE
        } else {
            View.GONE
        }
        rootView?.no_favorites_animation?.visibility = newVisibility
        rootView?.no_favorites_text?.visibility = newVisibility
    }
}
