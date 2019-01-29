package com.jorkoh.transportezaragozakt.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jorkoh.transportezaragozakt.R

class MoreFragment : Fragment() {

    companion object {
        const val DESTINATION_TAG = "MORE"

        @JvmStatic
        fun newInstance(): MoreFragment =
            MoreFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_more, container, false)
    }
}
