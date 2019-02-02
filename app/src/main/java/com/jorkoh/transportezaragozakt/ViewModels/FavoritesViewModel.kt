package com.jorkoh.transportezaragozakt.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.Models.BusStop.BusStopModel
import com.jorkoh.transportezaragozakt.Repositories.StopRepository

class FavoritesViewModel(private val stopRepository: StopRepository) : ViewModel() {
    private lateinit var stop: LiveData<BusStopModel>

    fun init(stopID : String){
        //Repository already injected by DI thanks to Koin
        stop = stopRepository.getStop(stopID)
    }

    fun getStop() : LiveData<BusStopModel>{
        return stop
    }
}