package com.jorkoh.transportezaragozakt.repositories

import android.location.Location
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import java.util.*

interface StopsRepository {
    fun loadStopDestinations(stopId: String, stopType: StopType): LiveData<Resource<List<StopDestination>>>
    fun loadStops(stopType: StopType): LiveData<List<Stop>>
    fun loadStops(): MediatorLiveData<List<Stop>>
    fun loadNearbyStops(location: LatLng, maxDistanceInMeters: Double): MediatorLiveData<List<StopWithDistance>>
    fun loadStopTitle(stopId: String): LiveData<String>
}

class StopsRepositoryImplementation(
    private val busRepository: BusRepository,
    private val tramRepository: TramRepository,
    private val stopsDao: StopsDao,
    private val appExecutors: AppExecutors
) : StopsRepository {

    override fun loadStopDestinations(stopId: String, stopType: StopType): LiveData<Resource<List<StopDestination>>> {
        return when (stopType) {
            StopType.BUS -> busRepository.loadStopDestinations(stopId)
            StopType.TRAM -> tramRepository.loadStopDestinations(stopId)
        }
    }

    override fun loadStops(stopType: StopType): LiveData<List<Stop>> {
        return when (stopType) {
            StopType.BUS -> busRepository.loadStops()
            StopType.TRAM -> tramRepository.loadStops()
        }
    }

    //TODO Check this out again
    override fun loadStops(): MediatorLiveData<List<Stop>> {
        val liveData = MediatorLiveData<List<Stop>>()
        liveData.value = listOf()
        liveData.addSource(busRepository.loadStops()) { newStops ->
            val newList = mutableListOf<Stop>()
            //Keep the old ones that were not of this type
            newList.addAll(liveData.value.orEmpty().filter { it.type != StopType.BUS } + newStops)
            liveData.value = newList
        }
        liveData.addSource(tramRepository.loadStops()) { newStops ->
            val newList = mutableListOf<Stop>()
            //Keep the old ones that were not of this type
            newList.addAll(liveData.value.orEmpty().filter { it.type != StopType.TRAM } + newStops)
            liveData.value = newList
        }
        return liveData
    }

    override fun loadNearbyStops(
        location: LatLng,
        maxDistanceInMeters: Double
    ): MediatorLiveData<List<StopWithDistance>> {
        val stopsOG = loadStops()
        val result = MediatorLiveData<List<StopWithDistance>>()
        result.value = listOf()
        stopsOG.observeForever { stops ->
            val newStopsWithDistance = mutableListOf<StopWithDistance>()
            val distance = FloatArray(1)
            stops.forEach { stop ->
                Location.distanceBetween(
                    location.latitude,
                    location.longitude,
                    stop.location.latitude,
                    stop.location.longitude,
                    distance
                )
                if (distance[0] < maxDistanceInMeters) {
                    newStopsWithDistance.add(StopWithDistance(stop, distance[0]))
                }
            }
            newStopsWithDistance.sortBy { it.distance }
            result.postValue(newStopsWithDistance)
        }
        return result
//        val liveData = MediatorLiveData<List<Stop>>()
//        liveData.value = listOf()
//        liveData.addSource(busRepository.loadStops()) { newStops ->
//            val newList = mutableListOf<Stop>()
//            val distance = FloatArray(1)
//            newList.addAll(liveData.value.orEmpty() + newStops.filter {
//                Location.distanceBetween(
//                    location.latitude,
//                    location.longitude,
//                    it.location.latitude,
//                    it.location.longitude,
//                    distance
//                )
//                distance[0] < maxDistanceInMeters
//            })
//            val removedDuplicates = newList.toSet()
//            liveData.value = removedDuplicates.toList()
//        }
//        liveData.addSource(tramRepository.loadStops()) { newStops ->
//            val newList = mutableListOf<Stop>()
//            val distance = FloatArray(1)
//            newList.addAll(liveData.value.orEmpty() + newStops.filter {
//                Location.distanceBetween(
//                    location.latitude,
//                    location.longitude,
//                    it.location.latitude,
//                    it.location.longitude,
//                    distance
//                )
//                distance[0] < maxDistanceInMeters
//            })
//            val removedDuplicates = newList.toSet()
//            liveData.value = removedDuplicates.toList()
//        }
//        return liveData
    }

    override fun loadStopTitle(stopId: String): LiveData<String> {
        return stopsDao.getStopTitle(stopId)
    }
}

// The list is fresh if its oldest member is fresh
fun List<StopDestination>.isFresh(timeoutInSeconds: Int) =
    (this.minBy { it.updatedAt }?.isFresh(timeoutInSeconds) ?: false)

// The destination is fresh if the time elapsed since the last update is less than the timeout
fun StopDestination.isFresh(timeoutInSeconds: Int): Boolean = ((Date().time - updatedAt.time) / 1000) < timeoutInSeconds