package com.jorkoh.transportezaragozakt.destinations.stop_details

import androidx.lifecycle.*
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.FavoritesRepository
import com.jorkoh.transportezaragozakt.repositories.RemindersRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import kotlinx.coroutines.launch
import java.util.*

class StopDetailsViewModel(
    val stopId: String,
    val stopType: StopType,
    private val stopsRepository: StopsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val remindersRepository: RemindersRepository
) :
    ViewModel() {

    private lateinit var tempStopDestinations: LiveData<Resource<List<StopDestination>>>
    val stopDestinations = MediatorLiveData<Resource<List<StopDestination>>>()

    val stopIsFavorited: LiveData<Boolean> = favoritesRepository.isFavoriteStop(stopId).asLiveData()
    val stop: LiveData<Stop> = stopsRepository.loadStop(stopType, stopId)

    fun toggleStopFavorite() {
        stop.value?.let { stop ->
            viewModelScope.launch {
                favoritesRepository.toggleStopFavorite(stop)
            }
        }
    }

    fun refreshStopDestinations() {
        if (::tempStopDestinations.isInitialized) {
            stopDestinations.removeSource(tempStopDestinations)
        }
        tempStopDestinations = stopsRepository.loadStopDestinations(stopType, stopId)
        stopDestinations.addSource(tempStopDestinations) { value ->
            stopDestinations.postValue(value)
        }
    }

    fun createReminder(daysOfWeek: List<Boolean>, time: Calendar) {
        remindersRepository.insertReminder(
            stopId,
            stopType,
            daysOfWeek,
            time.get(Calendar.HOUR_OF_DAY),
            time.get(Calendar.MINUTE)
        )
    }
}