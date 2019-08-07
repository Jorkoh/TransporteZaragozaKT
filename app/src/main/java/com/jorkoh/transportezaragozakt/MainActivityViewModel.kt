package com.jorkoh.transportezaragozakt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.jorkoh.transportezaragozakt.repositories.FavoritesRepository
import com.jorkoh.transportezaragozakt.repositories.RemindersRepository
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository

class MainActivityViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val remindersRepository: RemindersRepository,
    private val settingsRepository: SettingsRepository
) :
    ViewModel() {

    val currentNavController = MediatorLiveData<NavController>()
    var currentNavControllerSource: LiveData<NavController>? = null

    private var favoriteCount: Int = -1
    private var reminderCount: Int = -1

    lateinit var favoriteCountChange: LiveData<Int>
    lateinit var reminderCountChange: LiveData<Int>

    fun init() {
        favoriteCountChange = Transformations.map(favoritesRepository.loadFavoriteCount()) { newFavoriteCount ->
            if (favoriteCount < 0) {
                favoriteCount = newFavoriteCount
                0
            } else {
                val diff = newFavoriteCount - favoriteCount
                favoriteCount = newFavoriteCount
                diff
            }
        }

        reminderCountChange = Transformations.map(remindersRepository.loadReminderCount()) { newReminderCount ->
            if (reminderCount < 0) {
                reminderCount = newReminderCount
                0
            } else {
                val diff = newReminderCount - reminderCount
                reminderCount = newReminderCount
                diff
            }
        }

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