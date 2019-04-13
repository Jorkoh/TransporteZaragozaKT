package com.jorkoh.transportezaragozakt.destinations.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.repositories.FavoritesRepository


class FavoritesViewModel(private val favoritesRepository: FavoritesRepository): ViewModel() {

    private lateinit var favoriteStops: LiveData<List<FavoriteStopExtended>>

    fun init(){
        favoriteStops = favoritesRepository.loadFavoriteStops()
    }

    fun getFavoriteStops(): LiveData<List<FavoriteStopExtended>> {
        return favoriteStops
    }

    fun updateFavorite(alias : String, colorHex: String, stopId : String){
        favoritesRepository.updateFavorite(alias, colorHex, stopId)
    }

    fun restoreFavorite(stopId: String){
        favoritesRepository.restoreFavorite(stopId)
    }

    fun moveFavorite(from : Int, to : Int){
        favoritesRepository.moveFavorite(from, to)
    }
}