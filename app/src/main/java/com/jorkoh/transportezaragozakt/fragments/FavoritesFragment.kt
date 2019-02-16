package com.jorkoh.transportezaragozakt.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.jorkoh.transportezaragozakt.models.Bus.BusStop.BusStopModel
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.view_models.FavoritesViewModel
import kotlinx.android.synthetic.main.fragment_favorites.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    companion object {
        const val DESTINATION_TAG = "FAVORITES"
        const val STOP_ID_KEY = "STOP_ID_KEY"

        @JvmStatic
        fun newInstance(): FavoritesFragment =
            FavoritesFragment()
    }

    private val favoritesVM: FavoritesViewModel by viewModel()

    private val stopObserver = Observer<BusStopModel> { value -> value?.let { favorites_text.text = value.features.first().properties.title } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        favoritesVM.getStop().observe(this, stopObserver)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        favoritesVM.init(arguments?.getString(STOP_ID_KEY) ?: "")
    }

}
