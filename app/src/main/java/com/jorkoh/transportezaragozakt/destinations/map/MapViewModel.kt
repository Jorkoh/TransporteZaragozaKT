package com.jorkoh.transportezaragozakt.destinations.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.RuralRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.util.Resource

class MapViewModel(stopsRepository: StopsRepository, trackingsRepository: RuralRepository) :
    ViewModel() {

    val busStopLocations: LiveData<List<Stop>> = stopsRepository.loadStops(StopType.BUS)
    val tramStopLocations: LiveData<List<Stop>> = stopsRepository.loadStops(StopType.TRAM)
    //TESTING TRACKERS
    val ruralTrackings: LiveData<Resource<List<RuralTracking>>> = trackingsRepository.loadTrackings()

    val selectedStopId = MutableLiveData<String>()
}