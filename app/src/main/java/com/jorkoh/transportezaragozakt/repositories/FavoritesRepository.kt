package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.AppDatabase
import com.jorkoh.transportezaragozakt.db.FavoriteStopExtended
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopsDao

interface FavoritesRepository{
    fun loadFavoriteStops(): LiveData<List<FavoriteStopExtended>>
    fun isFavoriteStop(stopId: String): LiveData<Boolean>
    fun toggleStopFavorite(stopId: String)
    fun setFavoriteColor(colorHex: String, stopId : String)
}

class FavoritesRepositoryImplementation(private val stopsDao: StopsDao, private val db: AppDatabase, private val appExecutors: AppExecutors) : FavoritesRepository{
    override fun loadFavoriteStops(): LiveData<List<FavoriteStopExtended>> {
        return stopsDao.getFavoriteStops()
    }

    override fun isFavoriteStop(stopId: String): LiveData<Boolean> {
        return stopsDao.stopIsFavorite(stopId)
    }

    override fun toggleStopFavorite(stopId: String) {
        appExecutors.diskIO().execute {
            db.runInTransaction {
                stopsDao.toggleFavorite(stopId)
            }
        }
    }

    override fun setFavoriteColor(colorHex: String, stopId : String) {
        appExecutors.diskIO().execute {
            stopsDao.updateFavoriteColor(colorHex, stopId)
        }
    }
}