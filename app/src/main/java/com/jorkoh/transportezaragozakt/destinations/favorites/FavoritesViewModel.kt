package com.jorkoh.transportezaragozakt.destinations.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.util.Hex
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.repositories.FavoritesRepository


class FavoritesViewModel(private val favoritesRepository: FavoritesRepository): ViewModel() {

    private lateinit var favoriteStops: LiveData<List<FavoriteStopExtended>>

    fun init(){
        favoriteStops = favoritesRepository.loadFavoriteStops()
    }

    fun getFavoriteStops(): LiveData<List<FavoriteStopExtended>> {
        return favoriteStops
    }

    fun updateFavorite(colorHex: String, stopId : String){
        favoritesRepository.setFavoriteColor(colorHex, stopId)
    }
}