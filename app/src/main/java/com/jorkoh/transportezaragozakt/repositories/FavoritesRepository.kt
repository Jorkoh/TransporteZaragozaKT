package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.AppDatabase
import com.jorkoh.transportezaragozakt.db.FavoriteStop
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopsDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface FavoritesRepository{
    fun loadFavoriteStops(): LiveData<List<Stop>>
    fun isStopFavorited(stopId: String): LiveData<Boolean>
    fun toggleStopFavorite(stopId: String)
}

class FavoritesRepositoryImplementation(private val stopsDao: StopsDao, private val db: AppDatabase, private val appExecutors: AppExecutors) : FavoritesRepository{
    override fun loadFavoriteStops(): LiveData<List<Stop>> {
        return stopsDao.getFavoriteStops()
    }

    override fun isStopFavorited(stopId: String): LiveData<Boolean> {
        return stopsDao.stopIsFavorite(stopId)
    }

    override fun toggleStopFavorite(stopId: String) {
        appExecutors.diskIO().execute {
            db.runInTransaction {
                stopsDao.toggleFavorite(stopId)
            }
        }
    }

}