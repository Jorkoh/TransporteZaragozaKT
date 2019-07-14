package com.jorkoh.transportezaragozakt.destinations.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class MapViewModel(private val stopsRepository: StopsRepository, private val settingsRepository: SettingsRepository) :
    ViewModel() {

    val busStopLocations: LiveData<List<Stop>> = stopsRepository.loadStops(StopType.BUS)
    val tramStopLocations: LiveData<List<Stop>> = stopsRepository.loadStops(StopType.TRAM)

    //Settings
    val isDarkMap: LiveData<Boolean> = settingsRepository.loadIsDarkMap()
    val mapType: LiveData<Int> = settingsRepository.loadMapType()
    val trafficEnabled: LiveData<Boolean> = settingsRepository.loadTrafficEnabled()
    val busFilterEnabled: LiveData<Boolean> = settingsRepository.loadBusFilterEnabled()
    val tramFilterEnabled: LiveData<Boolean> = settingsRepository.loadTramFilterEnabled()


    fun setMapType(mapType: Int) {
        settingsRepository.setMapType(mapType)
    }


    fun setTrafficEnabled(enabled: Boolean) {
        settingsRepository.setTrafficEnabled(enabled)
    }

    fun setBusFilterEnabled(enabled: Boolean) {
        settingsRepository.setBusFilterEnabled(enabled)
    }


    fun setTramFilterEnabled(enabled: Boolean) {
        settingsRepository.setTramFilterEnabled(enabled)
    }
}