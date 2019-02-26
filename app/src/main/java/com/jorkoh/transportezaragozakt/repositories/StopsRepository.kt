package com.jorkoh.transportezaragozakt.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.db.StopsDao
import com.jorkoh.transportezaragozakt.services.api.APIService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface StopsRepository {
    fun getStopDestinations(stopId: String, stopType: StopType): LiveData<List<StopDestination>>
    fun getStopLocations(stopType: StopType): LiveData<List<Stop>>
    fun getFavoriteStops(): LiveData<List<Stop>>
    fun isStopFavorited(stopId: String): LiveData<Boolean>
    fun toggleStopFavorite(stopId: String)
}

class StopsRepositoryImplementation(
    private val stopsDao: StopsDao,
    private val busRepository: BusRepository,
    private val tramRepository: TramRepository
) : StopsRepository {
    override fun getStopDestinations(stopId: String, stopType: StopType): LiveData<List<StopDestination>> {
        return when (stopType) {
            StopType.BUS -> busRepository.getStopDestinations(stopId)
            StopType.TRAM -> tramRepository.getStopDestinations(stopId)
        }
    }

    override fun getStopLocations(stopType: StopType): LiveData<List<Stop>> {
        return when (stopType){
            StopType.BUS -> busRepository.getStopLocations()
            StopType.TRAM -> tramRepository.getStopLocations()
        }
    }

    override fun getFavoriteStops(): LiveData<List<Stop>> {
        return stopsDao.getFavoriteStops()
    }

    override fun isStopFavorited(stopId: String): LiveData<Boolean> {
        return stopsDao.stopIsFavorite(stopId)
    }

    override fun toggleStopFavorite(stopId: String) {
        GlobalScope.launch {
            stopsDao.toggleStopFavorite(stopId)
        }
    }

}