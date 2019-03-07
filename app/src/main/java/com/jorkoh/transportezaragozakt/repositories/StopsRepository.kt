package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopType

interface StopsRepository {
    fun loadStopDestinations(stopId: String, stopType: StopType): LiveData<Resource<List<StopDestination>>>
    fun loadStopLocations(stopType: StopType): LiveData<Resource<List<Stop>>>
}

class StopsRepositoryImplementation(
    private val busRepository: BusRepository,
    private val tramRepository: TramRepository
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
}