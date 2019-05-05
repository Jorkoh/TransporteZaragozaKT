package com.jorkoh.transportezaragozakt.destinations.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.repositories.FavoritesRepository
import com.jorkoh.transportezaragozakt.repositories.RemindersRepository


class FavoritesViewModel(private val favoritesRepository: FavoritesRepository): ViewModel() {

    lateinit var favoriteStops: LiveData<List<FavoriteStopExtended>>

    fun init(){
        favoriteStops = favoritesRepository.loadFavoriteStops()
    }

    fun updateFavorite(stopId : String, alias : String, colorHex: String){
        favoritesRepository.updateFavorite(stopId, alias, colorHex)
    }

    fun restoreFavorite(stopId: String){
        favoritesRepository.restoreFavorite(stopId)
    }

    fun moveFavorite(from : Int, to : Int){
        favoritesRepository.moveFavorite(from, to)
    }
}