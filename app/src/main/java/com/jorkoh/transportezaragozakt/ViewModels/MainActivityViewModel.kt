package com.jorkoh.transportezaragozakt.ViewModels

import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.MainActivity

class MainActivityViewModel : ViewModel() {
    private val customBackStack = mutableListOf<MainActivity.Destinations>()

    fun getCustomBackStack(): MutableList<MainActivity.Destinations> {
        return customBackStack
    }
}