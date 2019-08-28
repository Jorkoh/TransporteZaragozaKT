package com.jorkoh.transportezaragozakt.destinations.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class MapViewModel(stopsRepository: StopsRepository) :
    ViewModel() {

    val busStopLocations: LiveData<List<Stop>> = stopsRepository.loadStops(StopType.BUS)
    val tramStopLocations: LiveData<List<Stop>> = stopsRepository.loadStops(StopType.TRAM)

    val selectedStopId = MutableLiveData<String>()
}