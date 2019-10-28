package com.jorkoh.transportezaragozakt.destinations.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.jorkoh.transportezaragozakt.db.ReminderExtended
import com.jorkoh.transportezaragozakt.repositories.RemindersRepository
import kotlinx.coroutines.launch
import java.util.*

class RemindersViewModel(private val remindersRepository: RemindersRepository) : ViewModel() {

    val reminders = remindersRepository.getRemindersExtended().asLiveData()

    fun updateReminder(reminderId: Int, daysOfWeek: List<Boolean>, time: Calendar) {
        viewModelScope.launch {
            remindersRepository.updateReminder(reminderId, daysOfWeek, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE))
        }
    }

    fun updateReminder(reminderId: Int, alias: String, colorHex: String) {
        viewModelScope.launch {
            remindersRepository.updateReminder(reminderId, alias, colorHex)
        }
    }

    fun restoreReminder(reminder: ReminderExtended) {
        viewModelScope.launch {
            remindersRepository.restoreReminder(reminder)
        }
    }

    fun moveReminder(from: Int, to: Int) {
        viewModelScope.launch {
            remindersRepository.moveReminder(from, to)
        }
    }

    fun deleteReminder(reminderId: Int) {
        viewModelScope.launch {
            remindersRepository.removeReminder(reminderId)
        }
    }
}