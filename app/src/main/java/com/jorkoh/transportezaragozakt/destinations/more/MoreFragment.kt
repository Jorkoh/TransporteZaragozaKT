package com.jorkoh.transportezaragozakt.destinations.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.fragment_more.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoreFragment : Fragment() {

    companion object {
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
        val view = inflater.inflate(R.layout.fragment_more, container, false)

        view.themes_button.setOnClickListener{
            findNavController().navigate(R.id.action_more_to_cyaneaSettings)
        }

        return view
    }
}
