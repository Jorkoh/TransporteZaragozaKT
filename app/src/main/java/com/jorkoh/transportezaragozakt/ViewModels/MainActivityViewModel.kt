package com.jorkoh.transportezaragozakt.ViewModels

import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.Destinations

class MainActivityViewModel : ViewModel() {
    // @TODO: This breaks (?) the architecture since it holds some view related information with getFragment()
    private val myBackStack = mutableListOf<Destinations>()

    fun getCustomBackStack(): MutableList<Destinations> {
        return myBackStack
    }
}