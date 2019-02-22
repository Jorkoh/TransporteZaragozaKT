package com.jorkoh.transportezaragozakt.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.repositories.BusRepository
import com.jorkoh.transportezaragozakt.repositories.TramRepository

class MapViewModel(private val busRepository: BusRepository, private val tramRepository: TramRepository) : ViewModel() {

    private lateinit var busStopLocations: LiveData<List<Stop>>
    private lateinit var tramStopLocations: LiveData<List<Stop>>

    // @TODO: investigate how to do this properly
    var mapHasBeenStyled = false

    fun init() {
        //Repository already injected by DI thanks to Koin
        busStopLocations = busRepository.getStopLocations()
        tramStopLocations = tramRepository.getStopLocations()
    }

    fun getBusStopLocations(): LiveData<List<Stop>> {
        return busStopLocations
    }

    fun getTramStopLocations(): LiveData<List<Stop>> {
        return tramStopLocations
    }
}