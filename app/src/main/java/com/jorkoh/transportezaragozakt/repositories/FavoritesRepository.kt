package com.jorkoh.transportezaragozakt.repositories

import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.daos.FavoritesDao
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun getFavoriteStopsExtended(): Flow<List<FavoriteStopExtended>>
    fun getFavoriteCount(): Flow<Int>
    fun isFavoriteStop(stopId: String): Flow<Boolean>
    suspend fun toggleStopFavorite(stop: Stop)
    suspend fun updateFavorite(stopId: String, alias: String, colorHex: String)
    suspend fun restoreFavorite(favorite: FavoriteStopExtended)
    suspend fun moveFavorite(from: Int, to: Int)
    suspend fun removeFavorite(stopId: String)
    suspend fun deleteAllFavoriteStops()
}

class FavoritesRepositoryImplementation(
    private val favoritesDao: FavoritesDao
) : FavoritesRepository {
    override fun getFavoriteStopsExtended(): Flow<List<FavoriteStopExtended>> {
        return favoritesDao.getFavoriteStops()
    }

    override fun getFavoriteCount(): Flow<Int> {
        return favoritesDao.getFavoriteCount()
    }

    override fun isFavoriteStop(stopId: String): Flow<Boolean> {
        return favoritesDao.stopIsFavorite(stopId)
    }

    override suspend fun toggleStopFavorite(stop: Stop) {
        favoritesDao.toggleFavorite(stop)
    }

    override suspend fun updateFavorite(stopId: String, alias: String, colorHex: String) {
        favoritesDao.updateFavorite(stopId, colorHex, alias)
    }

    override suspend fun restoreFavorite(favorite: FavoriteStopExtended) {
        favoritesDao.updateFavorite(favorite.stopId, "", favorite.stopTitle)
    }

    override suspend fun moveFavorite(from: Int, to: Int) {
        favoritesDao.moveFavorite(from, to)
    }

    override suspend fun removeFavorite(stopId: String) {
        favoritesDao.removeFavorite(stopId)
    }

    override suspend fun deleteAllFavoriteStops() {
        favoritesDao.deleteAllFavorites()
    }
}
