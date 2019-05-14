package com.jorkoh.transportezaragozakt.destinations.more

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository

class MoreViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private lateinit var isDarkMap: LiveData<Boolean>

    fun init(){
        isDarkMap = settingsRepository.loadIsDarkMap()
    }

    fun getIsDarkMap(): LiveData<Boolean> {
        return isDarkMap
    }

    fun setIsDarkMode(isDarkMap: Boolean) {
        settingsRepository.setIsDarkMap(isDarkMap)
    }
}