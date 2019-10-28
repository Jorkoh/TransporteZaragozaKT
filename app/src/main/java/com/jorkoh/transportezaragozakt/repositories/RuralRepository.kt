package com.jorkoh.transportezaragozakt.repositories

import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.db.daos.StopsDao
import com.jorkoh.transportezaragozakt.db.daos.TrackingsDao
import com.jorkoh.transportezaragozakt.repositories.util.NetworkBoundResource
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiSuccessResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.CtazAPIService
import com.jorkoh.transportezaragozakt.services.ctaz_api.officialAPIToCtazAPIId
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.rural.RuralStopCtazAPIResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.rural.RuralTrackingsCtazAPIResponse
import kotlinx.coroutines.flow.Flow
import java.util.*

interface RuralRepository {
    fun loadTrackings(): Flow<Resource<List<RuralTracking>>>
    fun loadStop(busStopId: String): Flow<Stop>
    fun loadStopDestinations(ruralStopId: String): Flow<Resource<List<StopDestination>>>
    fun loadStops(): Flow<List<Stop>>
    fun loadStops(stopIds: List<String>): Flow<List<Stop>>
    fun loadMainLines(): Flow<List<Line>>
    fun loadLineLocations(lineId: String): Flow<List<LineLocation>>
    fun loadLine(lineId: String): Flow<Line>
    fun loadAlternativeLineIds(lineId: String): Flow<List<String>>
}

class RuralRepositoryImplementation(
    private val ctazAPIService: CtazAPIService,
    private val trackingsDao: TrackingsDao,
    private val stopsDao: StopsDao
) : RuralRepository {

    companion object {
        //Just to limit users a bit from spamming requests
        const val FRESH_TIMEOUT = 10
    }

    override fun loadTrackings(): Flow<Resource<List<RuralTracking>>> {
        return object : NetworkBoundResource<List<RuralTracking>, RuralTrackingsCtazAPIResponse>() {
            override fun processResponse(response: ApiSuccessResponse<RuralTrackingsCtazAPIResponse>): List<RuralTracking> {
                return response.body.toRuralTrackings()
            }

            override suspend fun saveCallResult(result: List<RuralTracking>) {
                trackingsDao.replaceTrackings(result)
            }

            override fun shouldFetch(data: List<RuralTracking>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(FRESH_TIMEOUT))
            }

            override suspend fun loadFromDb(): List<RuralTracking> = trackingsDao.getTrackings()

            override suspend fun fetchSource(): ApiResponse<RuralTrackingsCtazAPIResponse> = ctazAPIService.getRuralTrackings()
        }.asFlow()
    }

    override fun loadStopDestinations(ruralStopId: String): Flow<Resource<List<StopDestination>>> {
        return object : NetworkBoundResource<List<StopDestination>, RuralStopCtazAPIResponse>() {
            override fun processResponse(response: ApiSuccessResponse<RuralStopCtazAPIResponse>): List<StopDestination> {
                return response.body.toStopDestinations(ruralStopId)
            }

            override suspend fun saveCallResult(result: List<StopDestination>) {
                stopsDao.replaceStopDestinations(ruralStopId, result)
            }

            override fun shouldFetch(data: List<StopDestination>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(FRESH_TIMEOUT))
            }

            override suspend fun loadFromDb() = stopsDao.getStopDestinations(ruralStopId)

            override suspend fun fetchSource() = ctazAPIService.getRuralStopCtazAPI(ruralStopId.officialAPIToCtazAPIId())
        }.asFlow()
    }

    override fun loadStop(busStopId: String): Flow<Stop> {
        return stopsDao.getStop(busStopId)
    }

    override fun loadStops(): Flow<List<Stop>> {
        return stopsDao.getStopsByType(StopType.RURAL)
    }

    override fun loadStops(stopIds: List<String>): Flow<List<Stop>> {
        return stopsDao.getStops(stopIds)
    }

    override fun loadMainLines(): Flow<List<Line>> {
        return stopsDao.getMainLinesByType(LineType.RURAL)
    }

    override fun loadLineLocations(lineId: String): Flow<List<LineLocation>> {
        return stopsDao.getLineLocations(lineId)
    }

    override fun loadLine(lineId: String): Flow<Line> {
        return stopsDao.getLine(lineId)
    }

    override fun loadAlternativeLineIds(lineId: String): Flow<List<String>> {
        return stopsDao.getAlternativeLineIds(lineId)
    }
}

// The list is fresh if its oldest member is fresh
fun List<RuralTracking>.isFresh(timeoutInSeconds: Int) = (this.minBy { it.updatedAt }?.isFresh(timeoutInSeconds) ?: false)

// The destination is fresh if the time elapsed since the last update is less than the timeout
fun RuralTracking.isFresh(timeoutInSeconds: Int): Boolean = ((Date().time - updatedAt.time) / 1000) < timeoutInSeconds