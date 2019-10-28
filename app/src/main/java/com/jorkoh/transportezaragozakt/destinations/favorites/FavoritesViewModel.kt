package com.jorkoh.transportezaragozakt.destinations.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.repositories.FavoritesRepository
import kotlinx.coroutines.launch


class FavoritesViewModel(private val favoritesRepository: FavoritesRepository) : ViewModel() {

    val favoriteStops = favoritesRepository.getFavoriteStopsExtended().asLiveData()

    fun updateFavorite(stopId: String, alias: String, colorHex: String) {
        viewModelScope.launch {
            favoritesRepository.updateFavorite(stopId, alias, colorHex)
        }
    }

    fun restoreFavorite(favorite: FavoriteStopExtended) {
        viewModelScope.launch {
            favoritesRepository.restoreFavorite(favorite)
        }
    }

    fun moveFavorite(from: Int, to: Int) {
        viewModelScope.launch {
            favoritesRepository.moveFavorite(from, to)
        }
    }

    fun deleteFavorite(stopId: String) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(stopId)
        }
    }
}