package com.jorkoh.transportezaragozakt

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.repositories.FavoritesRepository
import com.jorkoh.transportezaragozakt.repositories.RemindersRepository

class MainActivityViewModel(private val favoritesRepository: FavoritesRepository, private val remindersRepository: RemindersRepository) :
    ViewModel() {

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
}