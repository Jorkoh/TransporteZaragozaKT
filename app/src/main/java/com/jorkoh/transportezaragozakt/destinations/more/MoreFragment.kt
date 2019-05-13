package com.jorkoh.transportezaragozakt.destinations.more

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jorkoh.transportezaragozakt.MainActivity
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.main_container.*
import kotlinx.android.synthetic.main.more_destination.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoreFragment : Fragment() {

    private val moreVM: MoreViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.more_destination, container, false)

        setupToolbar()

        view.themes_button.setOnClickListener{
            findNavController().navigate(MoreFragmentDirections.actionMoreToThemePicker())
        }

        return view
    }

    private fun setupToolbar(){
        requireActivity().main_toolbar.menu.clear()
    }
}
