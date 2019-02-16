package com.jorkoh.transportezaragozakt.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.models.IStop
import com.jorkoh.transportezaragozakt.models.StopType
import com.jorkoh.transportezaragozakt.repositories.BusRepository
import com.jorkoh.transportezaragozakt.repositories.TramRepository

class StopDetailsViewModel(private val busRepository: BusRepository, private val tramRepository: TramRepository) :
    ViewModel() {
    private lateinit var stop: LiveData<IStop>

    fun init(stopID: String, stopType : StopType) {
        stop = when(stopType){
            //@TODO Clean this up
            StopType.BUS -> busRepository.getStopInfo(stopID) as LiveData<IStop>
            StopType.TRAM -> tramRepository.getStopInfo(stopID) as LiveData<IStop>
        }
    }

    fun getStop(): LiveData<IStop> {
        return stop
    }
}