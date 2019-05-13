package com.jorkoh.transportezaragozakt.destinations.search

import android.util.Log
import android.widget.Filterable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.StopWithoutLocation
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class SearchViewModel(private val stopsRepository: StopsRepository) : ViewModel() {

    lateinit var query : MutableLiveData<String>

    lateinit var adapters : MutableList<Filterable>

    lateinit var allStops: LiveData<List<StopWithoutLocation>>

    fun init(){
        query = MutableLiveData("")
        allStops = stopsRepository.loadStops()
        adapters = mutableListOf()
        Log.d("TESTING STUFF", "SEARCH VIEW MODEL INITIALIZED")
    }

    fun filter(query : String?){
        adapters.forEach { it.filter.filter(query) }
    }
}