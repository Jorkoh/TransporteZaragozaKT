package com.jorkoh.transportezaragozakt.repositories

import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.db.daos.FavoritesDao
import com.jorkoh.transportezaragozakt.db.Stop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface FavoritesRepository {
    fun getFavoriteStops(): Flow<List<FavoriteStopExtended>>
    fun getFavoriteCount(): Flow<Int>
    fun isFavoriteStop(stopId: String): Flow<Boolean>
    suspend fun removeFavorite(stopId: String)
    suspend fun toggleStopFavorite(stop: Stop)
    fun updateFavorite(stopId: String, alias: String, colorHex: String)
    suspend fun restoreFavorite(favorite: FavoriteStopExtended)
    suspend fun moveFavorite(from: Int, to: Int)
    suspend fun deleteAllFavoriteStops()
}

class FavoritesRepositoryImplementation(
    private val favoritesDao: FavoritesDao,
    private val appExecutors: AppExecutors
) : FavoritesRepository {
    override fun getFavoriteStops(): Flow<List<FavoriteStopExtended>> {
        return favoritesDao.getFavoriteStops()
    }

    override fun isFavoriteStop(stopId: String): Flow<Boolean> {
        return favoritesDao.stopIsFavorite(stopId)
    }

    override suspend fun removeFavorite(stopId: String) {
        withContext(Dispatchers.IO) {
            favoritesDao.removeFavorite(stopId)
        }
    }

    override suspend fun toggleStopFavorite(stop: Stop) {
        withContext(Dispatchers.IO) {
            favoritesDao.toggleFavorite(stop)
        }
    }

    override fun updateFavorite(stopId: String, alias: String, colorHex: String) {
        appExecutors.diskIO().execute {
            favoritesDao.updateFavorite(stopId, colorHex, alias)
        }
    }

    override suspend fun restoreFavorite(favorite: FavoriteStopExtended) {
        withContext(Dispatchers.IO) {
            favoritesDao.updateFavorite(favorite.stopId, "", favorite.stopTitle)
        }
    }

    override suspend fun moveFavorite(from: Int, to: Int) {
        withContext(Dispatchers.IO) {
            favoritesDao.moveFavorite(from, to)
        }
    }

    override fun getFavoriteCount(): Flow<Int> {
        return favoritesDao.getFavoriteCount()
    }

    override suspend fun deleteAllFavoriteStops() {
        withContext(Dispatchers.IO) {
            favoritesDao.deleteAllFavorites()
        }
    }
}