package com.jorkoh.transportezaragozakt.destinations.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.more_destination.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoreFragment : Fragment() {

    private val moreVM: MoreViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.more_destination, container, false)

        view.themes_button.setOnClickListener{
            findNavController().navigate(MoreFragmentDirections.actionMoreToThemePicker())
        }

        return view
    }
}
