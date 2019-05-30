package com.jorkoh.transportezaragozakt.destinations.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class MapViewModel(private val stopsRepository: StopsRepository, private val settingsRepository: SettingsRepository) :
    ViewModel() {

    private lateinit var busStopLocations: LiveData<List<Stop>>
    private lateinit var tramStopLocations: LiveData<List<Stop>>

    //Settings
    lateinit var isDarkMap: LiveData<Boolean>
    lateinit var mapType: LiveData<Int>
    lateinit var trafficEnabled: LiveData<Boolean>
    lateinit var busFilterEnabled: LiveData<Boolean>
    lateinit var tramFilterEnabled: LiveData<Boolean>

    fun init() {
        isDarkMap = settingsRepository.loadIsDarkMap()
        mapType = settingsRepository.loadMapType()
        trafficEnabled = settingsRepository.loadTrafficEnabled()
        busFilterEnabled = settingsRepository.loadBusFilterEnabled()
        tramFilterEnabled = settingsRepository.loadTramFilterEnabled()
        busStopLocations = stopsRepository.loadStops(StopType.BUS)
        tramStopLocations = stopsRepository.loadStops(StopType.TRAM)
    }

    fun getBusStopLocations(): LiveData<List<Stop>> {
        return busStopLocations
    }

    fun getTramStopLocations(): LiveData<List<Stop>> {
        return tramStopLocations
    }

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