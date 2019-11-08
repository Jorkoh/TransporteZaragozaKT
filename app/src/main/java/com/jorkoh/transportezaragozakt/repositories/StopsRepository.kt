package com.jorkoh.transportezaragozakt.repositories

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.*

interface StopsRepository {
    fun loadStop(stopType: StopType, stopId: String): Flow<Stop>
    fun loadStopDestinations(stopType: StopType, stopId: String): Flow<Resource<List<StopDestination>>>
    fun loadStops(stopType: StopType): Flow<List<Stop>>
    fun loadStops(): Flow<List<Stop>>
    fun loadNearbyStops(location: LatLng, maxDistanceInMeters: Double): Flow<List<StopWithDistance>>
    fun loadMainLines(lineType: LineType): Flow<List<Line>>
    fun loadMainLines(): Flow<List<Line>>
    fun loadLineLocations(lineType: LineType, lineId: String): Flow<List<LineLocation>>
    fun loadLine(lineType: LineType, lineId: String): Flow<Line>
    fun loadAlternativeLineIds(lineType: LineType, lineId: String): Flow<List<String>>
    fun loadStops(stopType: StopType, stopIds: List<String>): Flow<List<Stop>>
}

class StopsRepositoryImplementation(
    private val busRepository: BusRepository,
    private val tramRepository: TramRepository,
    private val ruralRepository: RuralRepository
) : StopsRepository {

    override fun loadStop(stopType: StopType, stopId: String): Flow<Stop> {
        return when (stopType) {
            StopType.BUS -> busRepository.loadStop(stopId)
            StopType.TRAM -> tramRepository.loadStop(stopId)
            StopType.RURAL -> ruralRepository.loadStop(stopId)
        }
    }

    override fun loadStopDestinations(stopType: StopType, stopId: String): Flow<Resource<List<StopDestination>>> {
        return when (stopType) {
            StopType.BUS -> busRepository.loadStopDestinations(stopId)
            StopType.TRAM -> tramRepository.loadStopDestinations(stopId)
            StopType.RURAL -> ruralRepository.loadStopDestinations(stopId)
        }
    }

    override fun loadStops(stopType: StopType): Flow<List<Stop>> {
        return when (stopType) {
            StopType.BUS -> busRepository.loadStops()
            StopType.TRAM -> tramRepository.loadStops()
            StopType.RURAL -> ruralRepository.loadStops()
        }
    }

    override fun loadStops(stopType: StopType, stopIds: List<String>): Flow<List<Stop>> {
        return when (stopType) {
            StopType.BUS -> busRepository.loadStops(stopIds)
            StopType.TRAM -> tramRepository.loadStops(stopIds)
            StopType.RURAL -> ruralRepository.loadStops(stopIds)
        }
    }

    override fun loadMainLines(lineType: LineType): Flow<List<Line>> {
        return when (lineType) {
            LineType.BUS -> busRepository.loadMainLines()
            LineType.TRAM -> tramRepository.loadMainLines()
            LineType.RURAL -> ruralRepository.loadMainLines()
        }
    }

    override fun loadLineLocations(lineType: LineType, lineId: String): Flow<List<LineLocation>> {
        return when (lineType) {
            LineType.BUS -> busRepository.loadLineLocations(lineId)
            LineType.TRAM -> tramRepository.loadLineLocations(lineId)
            LineType.RURAL -> ruralRepository.loadLineLocations(lineId)
        }
    }

    override fun loadStops(): Flow<List<Stop>> {
        return loadStops(StopType.BUS).combine(loadStops(StopType.TRAM)) { bus, tram ->
            tram + bus
        }.combine(loadStops(StopType.RURAL)) { busAndTram, rural ->
            busAndTram + rural
        }
    }

    override fun loadMainLines(): Flow<List<Line>> {
        return loadMainLines(LineType.BUS).combine(loadMainLines(LineType.TRAM)) { bus, tram ->
            tram + bus
        }.combine(loadMainLines(LineType.RURAL)) { busAndTram, rural ->
            busAndTram + rural
        }
    }

    override fun loadLine(lineType: LineType, lineId: String): Flow<Line> {
        return when (lineType) {
            LineType.BUS -> busRepository.loadLine(lineId)
            LineType.TRAM -> tramRepository.loadLine(lineId)
            LineType.RURAL -> ruralRepository.loadLine(lineId)
        }
    }

    override fun loadAlternativeLineIds(lineType: LineType, lineId: String): Flow<List<String>> {
        return when (lineType) {
            LineType.BUS -> busRepository.loadAlternativeLineIds(lineId)
            LineType.TRAM -> tramRepository.loadAlternativeLineIds(lineId)
            LineType.RURAL -> ruralRepository.loadAlternativeLineIds(lineId)
        }
    }

    override fun loadNearbyStops(location: LatLng, maxDistanceInMeters: Double): Flow<List<StopWithDistance>> {
        return loadStops().map { stops ->
            val newStopsWithDistance = mutableListOf<StopWithDistance>()
            stops.forEach { stop ->
                val distance = SphericalUtil.computeDistanceBetween(location, stop.location).toFloat()
                if (distance < maxDistanceInMeters) {
                    newStopsWithDistance.add(StopWithDistance(stop, distance))
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