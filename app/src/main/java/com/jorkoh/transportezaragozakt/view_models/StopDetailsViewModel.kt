package com.jorkoh.transportezaragozakt.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.BusRepository
import com.jorkoh.transportezaragozakt.repositories.TramRepository

class StopDetailsViewModel(private val busRepository: BusRepository, private val tramRepository: TramRepository) :
    ViewModel() {

    private lateinit var stopID: String
    private lateinit var stopType: StopType
    private lateinit var stopDestinations: LiveData<List<StopDestination>>
    private lateinit var stopIsFavorited: LiveData<Boolean>

    fun init(stopID: String, stopType : StopType) {
        //TODO: :/
        this.stopID = stopID
        this.stopType = stopType

        stopDestinations = when(stopType){
            //@TODO either repository does kinda the same here :/
            StopType.BUS -> busRepository.getStopDestinations(stopID)
            StopType.TRAM -> tramRepository.getStopDestinations(stopID)
        }

        stopIsFavorited = when(stopType){
            //@TODO either repository does kinda the same here :/
            StopType.BUS -> busRepository.isStopFavorited(stopID)
            StopType.TRAM -> tramRepository.isStopFavorited(stopID)
        }

    }

    fun toggleStopFavorite(){
        when(stopType){
            //@TODO either repository does kinda the same here :/
            StopType.BUS -> busRepository.toggleStopFavorite(stopID)
            StopType.TRAM -> tramRepository.toggleStopFavorite(stopID)
        }
    }

    fun getStop(): LiveData<List<StopDestination>> {
        return stopDestinations
    }

    fun getStopIsFavorited() : LiveData<Boolean>{
        return stopIsFavorited
    }
}