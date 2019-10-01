package com.jorkoh.transportezaragozakt.destinations.map

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.RuralRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.repositories.util.Status

class MapViewModel(stopsRepository: StopsRepository, val trackingsRepository: RuralRepository) :
    ViewModel() {

    val busStopLocations = stopsRepository.loadStops(StopType.BUS)
    val tramStopLocations = stopsRepository.loadStops(StopType.TRAM)

    val ruralTrackings = MediatorLiveData<Resource<List<RuralTracking>>>()
    private lateinit var tempRuralTrackings: LiveData<Resource<List<RuralTracking>>>

    val selectedItemId = MutableLiveData<String>()

    private val handler = Handler()
    private val refreshTrackers = object : Runnable {
        override fun run() {
            if (::tempRuralTrackings.isInitialized) {
                ruralTrackings.removeSource(tempRuralTrackings)
            }
            tempRuralTrackings = trackingsRepository.loadTrackings()
            ruralTrackings.addSource(tempRuralTrackings) { value ->
                if (value.status == Status.SUCCESS && value.data != ruralTrackings.value) {
                    ruralTrackings.postValue(value)
                }
            }
            handler.postDelayed(this, 40000)
        }
    }

    init {
        handler.post(refreshTrackers)
    }
}