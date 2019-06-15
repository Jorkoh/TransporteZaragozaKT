package com.jorkoh.transportezaragozakt

import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository

class IntroActivityViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    fun isFirstLaunch(isFirstLaunch : Boolean) = settingsRepository.isFirstLaunch(isFirstLaunch)
}