package com.jorkoh.transportezaragozakt.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.services.api.models.StopType
import com.jorkoh.transportezaragozakt.repositories.BusRepository
import com.jorkoh.transportezaragozakt.repositories.TramRepository

class StopDetailsViewModel(private val busRepository: BusRepository, private val tramRepository: TramRepository) :
    ViewModel() {
    private lateinit var stopDestinations: LiveData<List<StopDestination>>

    fun init(stopID: String, stopType : StopType) {
        stopDestinations = when(stopType){
            //@TODO either repository does kinda the same here :/
            StopType.BUS -> busRepository.getStopDestinations(stopID)
            StopType.TRAM -> tramRepository.getStopDestinations(stopID)
        }
    }

    fun getStop(): LiveData<List<StopDestination>> {
        return stopDestinations
    }
}