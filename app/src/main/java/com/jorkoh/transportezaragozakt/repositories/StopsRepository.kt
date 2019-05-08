package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.db.StopsDao
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import java.util.*

interface StopsRepository {
    fun loadStopDestinations(stopId: String, stopType: StopType): LiveData<Resource<List<StopDestination>>>
    fun loadStopLocations(stopType: StopType): LiveData<Resource<List<Stop>>>
    fun loadStopTitle(stopId: String) : LiveData<String>
}

class StopsRepositoryImplementation(
    private val busRepository: BusRepository,
    private val tramRepository: TramRepository,
    private val stopsDao: StopsDao
    ) : StopsRepository {
    override fun loadStopDestinations(stopId: String, stopType: StopType): LiveData<Resource<List<StopDestination>>> {
        return when (stopType) {
            StopType.BUS -> busRepository.loadStopDestinations(stopId)
            StopType.TRAM -> tramRepository.loadStopDestinations(stopId)
        }
    }

    override fun loadStopLocations(stopType: StopType): LiveData<Resource<List<Stop>>> {
        return when (stopType){
            StopType.BUS -> busRepository.loadStopLocations()
            StopType.TRAM -> tramRepository.loadStopLocations()
        }
    }

    override fun loadStopTitle(stopId: String): LiveData<String> {
        return stopsDao.getStopTitle(stopId)
    }
}

// The list is fresh if its oldest member is fresh
fun List<StopDestination>.isFresh(timeoutInSeconds:Int) = (this.minBy { it.updatedAt }?.isFresh(timeoutInSeconds) ?: false)

// The destination is fresh if the time elapsed since the last update is less than the timeout
fun StopDestination.isFresh(timeoutInSeconds: Int): Boolean  = ((Date().time - updatedAt.time)/1000) < timeoutInSeconds