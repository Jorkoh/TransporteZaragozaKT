package com.jorkoh.transportezaragozakt.destinations.stop_details

import androidx.lifecycle.*
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.FavoritesRepository
import com.jorkoh.transportezaragozakt.repositories.RemindersRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class StopDetailsViewModel(
    val stopId: String,
    val stopType: StopType,
    private val stopsRepository: StopsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val remindersRepository: RemindersRepository
) : ViewModel() {

    val stopDestinations = MutableLiveData<Resource<List<StopDestination>>>()
    private var refreshJob: Job? = null

    val stopIsFavorited: LiveData<Boolean> = favoritesRepository.isFavoriteStop(stopId).asLiveData()
    val stop: LiveData<Stop> = stopsRepository.loadStop(stopType, stopId).asLiveData()

    init {
        refreshStopDestinations()
    }

    // Arrival times auto refresh every minute, the user can force an early refresh which causes the timer to restart
    fun refreshStopDestinations() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            stopsRepository.loadStopDestinations(stopType, stopId).collect {
                stopDestinations.postValue(it)
            }
            delay(60_000)
            refreshStopDestinations()
        }
    }

    fun toggleStopFavorite() {
        stop.value?.let { stop ->
            viewModelScope.launch {
                favoritesRepository.toggleStopFavorite(stop)
            }
        }
    }

    fun createReminder(daysOfWeek: List<Boolean>, time: Calendar) {
        viewModelScope.launch {
            remindersRepository.insertReminder(stopId, stopType, daysOfWeek, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE))
        }
    }
}