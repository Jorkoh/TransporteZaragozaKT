package com.jorkoh.transportezaragozakt.destinations.search

import android.util.Log
import android.widget.Filterable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.StopWithoutLocation
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class SearchViewModel(
    private val stopsRepository: StopsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val query: MutableLiveData<String?> = MutableLiveData()
    lateinit var allStops: LiveData<List<StopWithoutLocation>>

    private lateinit var tabPosition: LiveData<Int>

    fun init() {
        allStops = stopsRepository.loadStops()
        tabPosition = settingsRepository.loadSearchTabPosition()
    }

    fun getSearchTabPosition(): LiveData<Int> {
        return tabPosition
    }

    fun setSearchTabPosition(position: Int) {
        settingsRepository.setSearchTabPosition(position)
    }
}