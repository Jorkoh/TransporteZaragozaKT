package com.jorkoh.transportezaragozakt.destinations.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class MapViewModel(private val stopsRepository: StopsRepository, private val settingsRepository: SettingsRepository) :
    ViewModel() {

    private lateinit var busStopLocations: LiveData<List<Stop>>
    private lateinit var tramStopLocations: LiveData<List<Stop>>

    //Settings
    private lateinit var isDarkMap: LiveData<Boolean>
    private lateinit var mapType: LiveData<Int>
    private lateinit var trafficEnabled: LiveData<Boolean>
    private lateinit var busFilterEnabled: LiveData<Boolean>
    private lateinit var tramFilterEnabled: LiveData<Boolean>


    fun init() {
        //Repository already injected by DI thanks to Koin
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

    fun getIsDarkMap(): LiveData<Boolean> {
        return isDarkMap
    }

    fun getMapType(): LiveData<Int> {
        return mapType
    }

    fun setMapType(mapType: Int) {
        settingsRepository.setMapType(mapType)
    }

    fun getTrafficEnabled(): LiveData<Boolean> {
        return trafficEnabled
    }

    fun setTrafficEnabled(enabled: Boolean) {
        settingsRepository.setTrafficEnabled(enabled)
    }

    fun getBusFilterEnabled(): LiveData<Boolean> {
        return busFilterEnabled
    }

    fun setBusFilterEnabled(enabled: Boolean) {
        settingsRepository.setBusFilterEnabled(enabled)
    }

    fun getTramFilterEnabled(): LiveData<Boolean> {
        return tramFilterEnabled
    }

    fun setTramFilterEnabled(enabled: Boolean) {
        settingsRepository.setTramFilterEnabled(enabled)
    }
}