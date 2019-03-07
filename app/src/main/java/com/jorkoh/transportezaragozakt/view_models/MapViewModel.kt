package com.jorkoh.transportezaragozakt.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.Resource
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class MapViewModel(private val stopsRepository: StopsRepository) : ViewModel() {

    private lateinit var busStopLocations: LiveData<Resource<List<Stop>>>
    private lateinit var tramStopLocations: LiveData<Resource<List<Stop>>>

    // @TODO: investigate how to do this properly
    var mapHasBeenStyled = false

    fun init() {
        //Repository already injected by DI thanks to Koin
        busStopLocations = stopsRepository.loadStopLocations(StopType.BUS)
        tramStopLocations = stopsRepository.loadStopLocations(StopType.TRAM)
    }

    fun getBusStopLocations(): LiveData<Resource<List<Stop>>> {
        return busStopLocations
    }

    fun getTramStopLocations(): LiveData<Resource<List<Stop>>> {
        return tramStopLocations
    }
}