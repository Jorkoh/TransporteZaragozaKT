package com.jorkoh.transportezaragozakt.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.BusRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.TramRepository

class StopDetailsViewModel(private val stopsRepository: StopsRepository) :
    ViewModel() {

    private lateinit var stopID: String
    private lateinit var stopType:StopType
    private lateinit var stopDestinations: LiveData<List<StopDestination>>
    private lateinit var stopIsFavorited: LiveData<Boolean>

    fun init(stopID: String, stopType : StopType) {
        this.stopID = stopID
        this.stopType = stopType

        refreshStopDestinations()

        stopIsFavorited = stopsRepository.isStopFavorited(stopID)
    }

    fun toggleStopFavorite(){
        stopsRepository.toggleStopFavorite(stopID)
    }

    fun refreshStopDestinations() {
        stopDestinations = stopsRepository.getStopDestinations(stopID, stopType)
    }

    fun getStopDestinations(): LiveData<List<StopDestination>> {
        return stopDestinations
    }

    fun getStopIsFavorited() : LiveData<Boolean>{
        return stopIsFavorited
    }
}