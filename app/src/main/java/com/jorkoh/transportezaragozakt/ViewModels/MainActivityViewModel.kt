package com.jorkoh.transportezaragozakt.ViewModels

import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.Destinations
import com.jorkoh.transportezaragozakt.MainActivity

class MainActivityViewModel : ViewModel() {
    // @TODO: This breaks the architecture since it holds some view related information with getFragment()
    private val customBackStack = mutableListOf<Destinations>()

    fun getCustomBackStack(): MutableList<Destinations> {
        return customBackStack
    }
}