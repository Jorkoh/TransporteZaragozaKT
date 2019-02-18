package com.jorkoh.transportezaragozakt.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.view_models.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    companion object {
        const val DESTINATION_TAG = "SEARCH"

        @JvmStatic
        fun newInstance(): SearchFragment =
            SearchFragment()
    }

    private val searchVM: SearchViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }
}
