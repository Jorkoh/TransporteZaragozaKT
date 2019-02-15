package com.jorkoh.transportezaragozakt.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.Models.Bus.BusStopLocations.BusStopLocationsModel
import com.jorkoh.transportezaragozakt.Models.Tram.TramStopLocations.TramStopLocationsModel
import com.jorkoh.transportezaragozakt.Repositories.BusRepository
import com.jorkoh.transportezaragozakt.Repositories.TramRepository

class MapViewModel(private val busRepository: BusRepository, private val tramRepository: TramRepository) : ViewModel() {

    private lateinit var busStopLocations: LiveData<BusStopLocationsModel>
    private lateinit var tramStopLocations: LiveData<TramStopLocationsModel>

    // @TODO: investigate how to do this properly
    var mapHasBeenStyled = false

    fun init() {
        //Repository already injected by DI thanks to Koin
        busStopLocations = busRepository.getStopLocations()
        tramStopLocations = tramRepository.getStopLocations()
    }

    fun getBusStopLocations(): LiveData<BusStopLocationsModel> {
        return busStopLocations
    }

    fun getTramStopLocations(): LiveData<TramStopLocationsModel> {
        return tramStopLocations
    }
}