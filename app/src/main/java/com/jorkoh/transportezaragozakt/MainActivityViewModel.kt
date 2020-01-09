package com.jorkoh.transportezaragozakt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.jorkoh.transportezaragozakt.repositories.FavoritesRepository
import com.jorkoh.transportezaragozakt.repositories.RemindersRepository
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository
import kotlinx.coroutines.flow.transform

class MainActivityViewModel(
    favoritesRepository: FavoritesRepository,
    remindersRepository: RemindersRepository,
    private val settingsRepository: SettingsRepository
) :
    ViewModel() {

    val currentNavController = MediatorLiveData<NavController>()
    private var currentNavControllerSource: LiveData<NavController>? = null

    private var favoriteCount: Int = -1
    private var reminderCount: Int = -1

    val favoriteCountChangeSign = favoritesRepository.getFavoriteCount().transform { newFavoriteCount ->
        if (favoriteCount >= 0) {
            val diff = newFavoriteCount - favoriteCount
            if (diff != 0) {
                emit(diff > 0)
            }
        }
        favoriteCount = newFavoriteCount
    }
    val reminderCountChangeSign = remindersRepository.getReminderCount().transform { newReminderCount ->
        if (reminderCount >= 0) {
            val diff = newReminderCount - reminderCount
            if (diff != 0) {
                emit(diff > 0)
            }
        }
        reminderCount = newReminderCount
    }

    fun isFirstLaunch() = settingsRepository.isFirstLaunch()

    fun setController(newNavController: LiveData<NavController>) {
        currentNavControllerSource?.let {
            currentNavController.removeSource(it)
        }

        currentNavControllerSource = newNavController

        currentNavController.addSource(newNavController) {
            currentNavController.value = it
        }
    }
}