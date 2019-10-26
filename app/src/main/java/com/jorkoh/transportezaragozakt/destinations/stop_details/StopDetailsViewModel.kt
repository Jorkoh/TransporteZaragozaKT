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
    private val stopsRepository: StopsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val remindersRepository: RemindersRepository
) :
    ViewModel() {

    lateinit var stopId: String
    lateinit var stopType: StopType

    private lateinit var tempStopDestinations: LiveData<Resource<List<StopDestination>>>
    val stopDestinations = MediatorLiveData<Resource<List<StopDestination>>>()

    lateinit var stopIsFavorited: LiveData<Boolean>
    lateinit var stop: LiveData<Stop>

    fun init(stopId: String, stopType: StopType) {
        this.stopId = stopId
        this.stopType = stopType

        stopIsFavorited = favoritesRepository.isFavoriteStop(stopId).asLiveData()
        stop = stopsRepository.loadStop(stopType, stopId)
    }

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