package com.jorkoh.transportezaragozakt.destinations.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopWithDistance
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class SearchViewModel(
    private val stopsRepository: StopsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val query: MutableLiveData<String?> = MutableLiveData()
    lateinit var allStops: LiveData<List<Stop>>
    lateinit var nearbyStops: MediatorLiveData<List<StopWithDistance>>

    private lateinit var tabPosition: LiveData<Int>

    fun init() {
        allStops = stopsRepository.loadStops()
        nearbyStops = stopsRepository.loadNearbyStops(LatLng(41.667971, -0.890905), 500.0)
        tabPosition = settingsRepository.loadSearchTabPosition()
    }

    fun getSearchTabPosition(): LiveData<Int> {
        return tabPosition
    }

    fun setSearchTabPosition(position: Int) {
        settingsRepository.setSearchTabPosition(position)
    }
}