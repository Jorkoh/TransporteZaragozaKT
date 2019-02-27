package com.jorkoh.transportezaragozakt.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.activities.MainActivity
import com.jorkoh.transportezaragozakt.adapters.FavoriteStopsAdapter
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.TagInfo
import com.jorkoh.transportezaragozakt.view_models.FavoritesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.android.synthetic.main.fragment_favorites.view.*

class FavoritesFragment : Fragment() {

    companion object {
        const val DESTINATION_TAG = "FAVORITES"

        @JvmStatic
        fun newInstance(): FavoritesFragment =
            FavoritesFragment()
    }

    private val favoritesVM: FavoritesViewModel by viewModel()

    private val itemOnClick: (TagInfo) -> Unit = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            (activity as MainActivity).openStopDetails(info)
        }
    }

    private val itemOnLongClick: (TagInfo) -> Boolean = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            (activity as MainActivity).openStopDetails(info)
        }
        true
    }

    private val favoriteStopsAdapter: FavoriteStopsAdapter = FavoriteStopsAdapter(itemOnClick, itemOnLongClick)

    private val favoriteStopsObserver = Observer<List<Stop>> { value ->
        value?.let {
            favoriteStopsAdapter.setFavoriteStops(value)
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
        }

        favoritesVM.getFavoriteStops().observe(this, favoriteStopsObserver)
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favoritesVM.init()
    }

}
