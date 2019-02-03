package com.jorkoh.transportezaragozakt.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.Models.BusStopLocations.BusStopLocationsModel
import com.jorkoh.transportezaragozakt.Repositories.StopRepository

class MapViewModel(private val stopRepository: StopRepository) : ViewModel() {

    private lateinit var stopLocations: LiveData<BusStopLocationsModel>

    // @TODO: investigate how to do this properly
    var mapHasBeenStyled = false

    fun init(){
        //Repository already injected by DI thanks to Koin
        stopLocations = stopRepository.getStopLocations()
    }

    fun getStopLocations() : LiveData<BusStopLocationsModel> {
        return stopLocations
    }
}