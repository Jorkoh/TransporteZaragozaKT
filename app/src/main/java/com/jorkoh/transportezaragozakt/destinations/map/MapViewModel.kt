package com.jorkoh.transportezaragozakt.destinations.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.RuralRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.util.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

class MapViewModel(stopsRepository: StopsRepository, trackingsRepository: RuralRepository) :
    ViewModel() {

    val busStopLocations = stopsRepository.loadStops(StopType.BUS)
    val tramStopLocations = stopsRepository.loadStops(StopType.TRAM)
    val ruralStopLocations = stopsRepository.loadStops(StopType.RURAL)

    val ruralTrackings: LiveData<List<RuralTracking>?> = liveData {
        while (true) {
            trackingsRepository.loadTrackings().collect { trackings ->
                if (trackings.status == Status.SUCCESS) {
                    emit(trackings.data)
                }
            }
            delay(30000)
        }
    }

    val selectedItemId = MutableLiveData<String>()
}