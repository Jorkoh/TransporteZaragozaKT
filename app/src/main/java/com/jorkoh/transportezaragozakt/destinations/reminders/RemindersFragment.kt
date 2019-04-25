package com.jorkoh.transportezaragozakt.destinations.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jorkoh.transportezaragozakt.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemindersFragment : Fragment() {

    private val remindersVM: RemindersViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.reminders_destination, container, false)

        return view
    }
}
