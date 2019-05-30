package com.jorkoh.transportezaragozakt.destinations.search

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.Line
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopWithDistance
import com.jorkoh.transportezaragozakt.destinations.map.toLatLng
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class SearchViewModel(
    private val stopsRepository: StopsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val query: MutableLiveData<String?> = MutableLiveData()

    val position: MutableLiveData<Location> = MutableLiveData()

    lateinit var allStops: LiveData<List<Stop>>
    lateinit var nearbyStops: LiveData<List<StopWithDistance>>
    lateinit var lines: MutableLiveData<List<Line>>

    private lateinit var tabPosition: LiveData<Int>

    fun init() {
        allStops = stopsRepository.loadStops()
        lines = stopsRepository.loadLines()
        tabPosition = settingsRepository.loadSearchTabPosition()
        nearbyStops = Transformations.switchMap(position){
            stopsRepository.loadNearbyStops(it.toLatLng(), 500.0)
        }
    }

    fun getSearchTabPosition(): LiveData<Int> {
        return tabPosition
    }

    fun setSearchTabPosition(position: Int) {
        settingsRepository.setSearchTabPosition(position)
    }
}