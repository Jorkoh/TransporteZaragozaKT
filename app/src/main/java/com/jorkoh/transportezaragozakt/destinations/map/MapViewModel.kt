package com.jorkoh.transportezaragozakt.destinations.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.repositories.Resource
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class MapViewModel(private val stopsRepository: StopsRepository, private val settingsRepository: SettingsRepository) : ViewModel() {

    private lateinit var busStopLocations: LiveData<Resource<List<Stop>>>
    private lateinit var tramStopLocations: LiveData<Resource<List<Stop>>>
    private lateinit var mapType: LiveData<Int>

    // @TODO: investigate how to do this properly
    var mapHasBeenStyled = false

    fun init() {
        //Repository already injected by DI thanks to Koin
        busStopLocations = stopsRepository.loadStopLocations(StopType.BUS)
        tramStopLocations = stopsRepository.loadStopLocations(StopType.TRAM)
        mapType = MutableLiveData(GoogleMap.MAP_TYPE_NORMAL)
        mapType = settingsRepository.loadMapType()
    }

    fun getBusStopLocations(): LiveData<Resource<List<Stop>>> {
        return busStopLocations
    }

    fun getTramStopLocations(): LiveData<Resource<List<Stop>>> {
        return tramStopLocations
    }

    fun getMapType() : LiveData<Int> {
        return mapType
    }

    fun setMapType(mapType : Int){
        settingsRepository.setMapType(mapType)
    }
}