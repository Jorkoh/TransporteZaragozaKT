package com.jorkoh.transportezaragozakt.destinations.utils

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.ui.setupWithNavController
import com.jorkoh.transportezaragozakt.MainActivityViewModel
import com.jorkoh.transportezaragozakt.R
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

open class FragmentWithToolbar : Fragment(){

    private val mainActivityVM: MainActivityViewModel by sharedViewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivityVM.currentNavController.observe(viewLifecycleOwner, Observer {navController ->
            view?.findViewById<Toolbar>(R.id.fragment_toolbar)?.setupWithNavController(navController)
        })
    }
}