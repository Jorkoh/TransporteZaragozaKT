package com.jorkoh.transportezaragozakt.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.jorkoh.transportezaragozakt.Models.BusStopModel
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.ViewModels.FavoritesViewModel
import kotlinx.android.synthetic.main.fragment_favorites.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    companion object {
        const val TAG = "FavoritesFragment"
        const val STOP_ID_KEY = "STOP_ID_KEY"

        @JvmStatic
        fun newInstance(): FavoritesFragment =
            FavoritesFragment()
    }

    private val favoritesViewModel: FavoritesViewModel by viewModel()

    private val stopObserver = Observer<BusStopModel> { value -> value?.let { favorites_text.text = value.features.first().properties.title } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set-up observer for the live data, init the viewmodel with an id passed as argument to the fragment
        favoritesViewModel.init(arguments?.getString(STOP_ID_KEY) ?: "")
        favoritesViewModel.getStop().observe(this, stopObserver)
    }

}
