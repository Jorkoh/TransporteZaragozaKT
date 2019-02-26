package com.jorkoh.transportezaragozakt.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.repositories.BusRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.TramRepository


class FavoritesViewModel(private val stopsRepository: StopsRepository): ViewModel() {

    private lateinit var favoriteStops: LiveData<List<Stop>>

    fun init(){
        favoriteStops = stopsRepository.getFavoriteStops()
    }

    fun getFavoriteStops(): LiveData<List<Stop>> {
        return favoriteStops
    }
}