package com.jorkoh.transportezaragozakt.destinations.more

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.main_container.*
import kotlinx.android.synthetic.main.more_destination.*
import kotlinx.android.synthetic.main.more_destination.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoreFragment : Fragment() {

    private val moreVM: MoreViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        moreVM.init()
        moreVM.getIsDarkMap().observe(viewLifecycleOwner, Observer { isDarkMap ->
            dark_map_switch.isChecked = isDarkMap
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.more_destination, container, false)

        setupToolbar()

        view.themes_button.setOnClickListener {
            findNavController().navigate(MoreFragmentDirections.actionMoreToThemePicker())
        }
        view.dark_map_switch.setOnCheckedChangeListener { _, isChecked ->
            moreVM.setIsDarkMode(isChecked)
        }

        return view
    }

    private fun setupToolbar() {
        requireActivity().main_toolbar.menu.clear()
    }
}
