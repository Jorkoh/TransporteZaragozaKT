package com.jorkoh.transportezaragozakt.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.activities.MainActivity
import com.jorkoh.transportezaragozakt.adapters.FavoriteStopsAdapter
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.TagInfo
import com.jorkoh.transportezaragozakt.view_models.FavoritesViewModel
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_favorites.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

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

    //TODO: TESTING
    private val itemOnLongClick: (TagInfo) -> Boolean = { _ ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Toast.makeText(context, "Long press", Toast.LENGTH_LONG).show()
        }
        true
    }

    private val favoriteStopsAdapter: FavoriteStopsAdapter = FavoriteStopsAdapter(itemOnClick, itemOnLongClick)

    private val favoriteStopsObserver = Observer<List<Stop>> { value ->
        value?.let {
            if(value.count() == 0){
                no_favorites_animation.visibility = View.VISIBLE
                no_favorites_text.visibility = View.VISIBLE
            }else{
                no_favorites_animation.visibility = View.GONE
                no_favorites_text.visibility = View.GONE
            }
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
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        favoritesVM.getFavoriteStops().observe(this, favoriteStopsObserver)
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favoritesVM.init()
    }

}
