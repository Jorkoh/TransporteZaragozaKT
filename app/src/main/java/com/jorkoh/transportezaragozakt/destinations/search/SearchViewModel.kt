package com.jorkoh.transportezaragozakt.destinations.search

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.Line
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopWithDistance
import com.jorkoh.transportezaragozakt.destinations.toLatLng
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class SearchViewModel(
    private val stopsRepository: StopsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val query: MutableLiveData<String?> = MutableLiveData()
    val position: MutableLiveData<Location> = MutableLiveData()
    val searchTabPosition = settingsRepository.loadSearchTabPosition()

    val nearbyStops: LiveData<List<StopWithDistance>> = Transformations.switchMap(position){
        stopsRepository.loadNearbyStops(it.toLatLng(), 500.0)
    }
    val allStops: LiveData<List<Stop>> = stopsRepository.loadStops()
    val mainLines: MutableLiveData<List<Line>> = stopsRepository.loadMainLines()


    fun setSearchTabPosition(position: Int) {
        settingsRepository.setSearchTabPosition(position)
    }
}