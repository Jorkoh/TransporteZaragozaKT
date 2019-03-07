package com.jorkoh.transportezaragozakt.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.view_models.MoreViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoreFragment : Fragment() {

    companion object {
        const val DESTINATION_TAG = "MORE"

        @JvmStatic
        fun newInstance(): MoreFragment =
            MoreFragment()
    }

    private val moreVM: MoreViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_more, container, false)
    }
}
