package com.jorkoh.transportezaragozakt.repositories

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.util.CombinedLiveData
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import java.util.*

interface StopsRepository {
    fun loadStop(stopType: StopType, stopId: String): LiveData<Stop>
    fun loadStopDestinations(stopType: StopType, stopId: String): LiveData<Resource<List<StopDestination>>>
    fun loadStops(stopType: StopType): LiveData<List<Stop>>
    fun loadStops(): MediatorLiveData<List<Stop>>
    fun loadNearbyStops(location: LatLng, maxDistanceInMeters: Double): LiveData<List<StopWithDistance>>
    fun loadLines(lineType: LineType): LiveData<List<Line>>
    fun loadLines(): MutableLiveData<List<Line>>
    fun loadLineLocations(lineType: LineType, lineId: String): LiveData<List<LineLocation>>
    fun loadLine(lineType: LineType, lineId: String): LiveData<Line>
    fun loadStops(stopType: StopType, stopIds: List<String>): LiveData<List<Stop>>
}

class StopsRepositoryImplementation(
    private val busRepository: BusRepository,
    private val tramRepository: TramRepository,
    private val ruralRepository: RuralRepository
) : StopsRepository {

    override fun loadStop(stopType: StopType, stopId: String): LiveData<Stop> {
        return when (stopType){
            StopType.BUS -> busRepository.loadStop(stopId)
            StopType.TRAM -> tramRepository.loadStop(stopId)
            StopType.RURAL -> TODO()
        }
    }

    override fun loadStopDestinations(stopType: StopType, stopId: String): LiveData<Resource<List<StopDestination>>> {
        return when (stopType) {
            StopType.BUS -> busRepository.loadStopDestinations(stopId)
            StopType.TRAM -> tramRepository.loadStopDestinations(stopId)
            StopType.RURAL -> TODO()
        }
    }

    override fun loadStops(stopType: StopType): LiveData<List<Stop>> {
        return when (stopType) {
            StopType.BUS -> busRepository.loadStops()
            StopType.TRAM -> tramRepository.loadStops()
            StopType.RURAL -> TODO()
        }
    }

    override fun loadStops(stopType: StopType, stopIds: List<String>): LiveData<List<Stop>> {
        return when (stopType) {
            StopType.BUS -> busRepository.loadStops(stopIds)
            StopType.TRAM -> tramRepository.loadStops(stopIds)
            StopType.RURAL -> TODO()
        }
    }

    override fun loadLines(lineType: LineType): LiveData<List<Line>> {
        return when (lineType) {
            LineType.BUS -> busRepository.loadLines()
            LineType.TRAM -> tramRepository.loadLines()
            LineType.RURAL -> TODO()
        }
    }

    override fun loadLineLocations(lineType: LineType, lineId: String): LiveData<List<LineLocation>> {
        return when (lineType) {
            LineType.BUS -> busRepository.loadLineLocations(lineId)
            LineType.TRAM -> tramRepository.loadLineLocations(lineId)
            LineType.RURAL -> TODO()
        }
    }

    override fun loadStops(): MediatorLiveData<List<Stop>> {
        return CombinedLiveData(
            loadStops(StopType.BUS),
            loadStops(StopType.TRAM)
        ) { bus, tram -> tram.orEmpty() + bus.orEmpty() }
    }

    override fun loadLines(): MutableLiveData<List<Line>> {
        return CombinedLiveData(
            loadLines(LineType.BUS),
            loadLines(LineType.TRAM)
        ) { bus, tram -> tram.orEmpty() + bus.orEmpty() }
    }

    override fun loadLine(lineType: LineType, lineId: String): LiveData<Line> {
        return when (lineType) {
            LineType.BUS -> busRepository.loadLine(lineId)
            LineType.TRAM -> tramRepository.loadLine(lineId)
            LineType.RURAL -> TODO()
        }
    }

    override fun loadNearbyStops(
        location: LatLng,
        maxDistanceInMeters: Double
    ): LiveData<List<StopWithDistance>> {
        return Transformations.map(loadStops()) { stops ->
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
            newStopsWithDistance.sortedBy { it.distance }
        }
    }
}

// The list is fresh if its oldest member is fresh
fun List<StopDestination>.isFresh(timeoutInSeconds: Int) =
    (this.minBy { it.updatedAt }?.isFresh(timeoutInSeconds) ?: false)

// The destination is fresh if the time elapsed since the last update is less than the timeout
fun StopDestination.isFresh(timeoutInSeconds: Int): Boolean = ((Date().time - updatedAt.time) / 1000) < timeoutInSeconds