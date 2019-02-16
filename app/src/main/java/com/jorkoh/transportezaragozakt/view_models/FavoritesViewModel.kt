package com.jorkoh.transportezaragozakt.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.models.Bus.BusStop.BusStopModel
import com.jorkoh.transportezaragozakt.repositories.BusRepository

class FavoritesViewModel(private val busRepository: BusRepository) : ViewModel() {
    private lateinit var stop: LiveData<BusStopModel>

    fun init(stopID : String){
        //Repository already injected by DI thanks to Koin
        stop = busRepository.getStopInfo(stopID)
    }

    fun getStop() : LiveData<BusStopModel>{
        return stop
    }
}