package com.jorkoh.transportezaragozakt.destinations.reminders

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.Reminder
import com.jorkoh.transportezaragozakt.db.ReminderExtended
import com.jorkoh.transportezaragozakt.repositories.RemindersRepository
import java.util.*

class RemindersViewModel(private val remindersRepository: RemindersRepository) : ViewModel() {

    lateinit var reminders: LiveData<List<ReminderExtended>>

    fun init(){
        reminders = remindersRepository.loadReminders()
    }

    fun updateReminder(reminderId : String, daysOfWeek: List<Boolean>, time : Calendar){
        remindersRepository.updateReminder(reminderId, daysOfWeek, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE))
    }

    fun updateReminder(reminderId : String, alias : String, colorHex: String){
        remindersRepository.updateReminder(reminderId, alias, colorHex)
    }

    fun restoreReminder(reminderId: String, stopId : String){
        remindersRepository.restoreReminder(reminderId, stopId)
    }

    fun moveReminder(from : Int, to : Int){
        remindersRepository.moveReminder(from, to)
    }

    fun deleteReminder(reminderId : String){
        remindersRepository.deleteReminder(reminderId)
    }
}