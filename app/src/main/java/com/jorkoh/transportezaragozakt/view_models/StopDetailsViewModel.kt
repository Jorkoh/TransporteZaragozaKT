package com.jorkoh.transportezaragozakt.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.FavoritesRepository
import com.jorkoh.transportezaragozakt.repositories.Resource
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class StopDetailsViewModel(
    private val stopsRepository: StopsRepository,
    private val favoritesRepository: FavoritesRepository
) :
    ViewModel() {

    private lateinit var stopID: String
    private lateinit var stopType: StopType

    //Not quite sold on this but I trust this guy https://medium.com/@BladeCoder/to-implement-a-manual-refresh-without-modifying-your-existing-livedata-logic-i-suggest-that-your-7db1b8414c0e
    private val mediatorStopDestinations = MediatorLiveData<Resource<List<StopDestination>>>()
    private lateinit var stopDestinations: LiveData<Resource<List<StopDestination>>>
    lateinit var stopIsFavorited: LiveData<Boolean>

    fun init(stopID: String, stopType: StopType) {
        this.stopID = stopID
        this.stopType = stopType

        refreshStopDestinations()

        stopIsFavorited = favoritesRepository.isStopFavorited(stopID)
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
}