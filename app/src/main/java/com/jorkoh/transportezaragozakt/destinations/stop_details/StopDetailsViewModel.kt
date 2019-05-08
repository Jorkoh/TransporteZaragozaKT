package com.jorkoh.transportezaragozakt.destinations.stop_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.FavoritesRepository
import com.jorkoh.transportezaragozakt.repositories.RemindersRepository
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import java.util.*

class StopDetailsViewModel(
    private val stopsRepository: StopsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val remindersRepository: RemindersRepository
) :
    ViewModel() {

    lateinit var stopID: String
    lateinit var stopType: StopType

    //Not quite sold on this but I trust this guy https://medium.com/@BladeCoder/to-implement-a-manual-refresh-without-modifying-your-existing-livedata-logic-i-suggest-that-your-7db1b8414c0e
    private val mediatorStopDestinations = MediatorLiveData<Resource<List<StopDestination>>>()
    private lateinit var stopDestinations: LiveData<Resource<List<StopDestination>>>
    lateinit var stopIsFavorited: LiveData<Boolean>
    lateinit var stopTitle: LiveData<String>

    fun init(stopID: String, stopType: StopType) {
        this.stopID = stopID
        this.stopType = stopType

        refreshStopDestinations()

        stopIsFavorited = favoritesRepository.isFavoriteStop(stopID)
        stopTitle = stopsRepository.loadStopTitle(stopID)
    }

    fun toggleStopFavorite() {
        favoritesRepository.toggleStopFavorite(stopID)
    }

    fun refreshStopDestinations() {
        if (::stopDestinations.isInitialized) {
            mediatorStopDestinations.removeSource(stopDestinations)
        }
        stopDestinations = stopsRepository.loadStopDestinations(stopID, stopType)
        mediatorStopDestinations.addSource(stopDestinations)
        { value ->
            mediatorStopDestinations.value = value
        }
    }

    fun getStopDestinations(): LiveData<Resource<List<StopDestination>>> {
        return mediatorStopDestinations
    }

    fun createReminder(daysOfWeek: List<Boolean>, time : Calendar){
        remindersRepository.insertReminder(stopID, stopType, daysOfWeek, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE))
    }
}