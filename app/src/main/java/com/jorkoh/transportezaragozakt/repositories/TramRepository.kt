package com.jorkoh.transportezaragozakt.repositories

import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.db.daos.StopsDao
import com.jorkoh.transportezaragozakt.repositories.util.NetworkBoundResourceWithBackup
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.services.common.util.ApiSuccessResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.CtazAPIService
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.tram.TramStopCtazAPIResponse
import com.jorkoh.transportezaragozakt.services.official_api.OfficialAPIService
import com.jorkoh.transportezaragozakt.services.official_api.responses.tram.TramStopOfficialAPIResponse
import com.jorkoh.transportezaragozakt.services.tram_api.TramAPIService
import com.jorkoh.transportezaragozakt.services.tram_api.officialAPIToTramAPIId
import com.jorkoh.transportezaragozakt.services.tram_api.responses.tram.TramStopTramAPIResponse
import kotlinx.coroutines.flow.Flow


interface TramRepository {
    fun loadStop(tramStopId: String): Flow<Stop>
    fun loadStopDestinations(tramStopId: String): Flow<Resource<List<StopDestination>>>
    fun loadStops(): Flow<List<Stop>>
    fun loadStops(stopIds: List<String>): Flow<List<Stop>>
    fun loadMainLines(): Flow<List<Line>>
    fun loadLineLocations(lineId: String): Flow<List<LineLocation>>
    fun loadLine(lineId: String): Flow<Line>
    fun loadAlternativeLineIds(lineId: String): Flow<List<String>>
}

class TramRepositoryImplementation(
    private val officialApiService: OfficialAPIService,
    private val tramAPIService: TramAPIService,
    private val ctazAPIService: CtazAPIService,
    private val stopsDao: StopsDao
) : TramRepository {

    companion object {
        //Just to limit users a bit from spamming requests
        const val FRESH_TIMEOUT = 10
    }

    override fun loadStopDestinations(tramStopId: String): Flow<Resource<List<StopDestination>>> {
        return object :
            NetworkBoundResourceWithBackup<List<StopDestination>, TramStopOfficialAPIResponse, TramStopTramAPIResponse, TramStopCtazAPIResponse>() {
            override fun processPrimaryResponse(response: ApiSuccessResponse<TramStopOfficialAPIResponse>): List<StopDestination> {
                return response.body.toStopDestinations(tramStopId)
            }

            override fun processSecondaryResponse(response: ApiSuccessResponse<TramStopTramAPIResponse>): List<StopDestination> {
                return response.body.toStopDestinations(tramStopId)
            }

            override fun processTertiaryResponse(response: ApiSuccessResponse<TramStopCtazAPIResponse>): List<StopDestination> {
                return response.body.toStopDestinations(tramStopId)
            }

            override suspend fun saveCallResult(result: List<StopDestination>) {
                stopsDao.replaceStopDestinations(tramStopId, result)
            }

            override fun shouldFetch(data: List<StopDestination>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(FRESH_TIMEOUT))
            }

            override suspend fun loadFromDb() = stopsDao.getStopDestinations(tramStopId)

            override suspend fun fetchPrimarySource() = officialApiService.getTramStopOfficialAPI(tramStopId)

            override suspend fun fetchSecondarySource() = tramAPIService.getTramStopTramAPI(tramStopId.officialAPIToTramAPIId())

            override suspend fun fetchTertiarySource() = ctazAPIService.getTramStopCtazAPI(tramStopId)
        }.asFlow()
    }

    override fun loadStop(tramStopId: String): Flow<Stop> {
        return stopsDao.getStop(tramStopId)
    }

    override fun loadStops(): Flow<List<Stop>> {
        return stopsDao.getStopsByType(StopType.TRAM)
    }

    override fun loadStops(stopIds: List<String>): Flow<List<Stop>> {
        return stopsDao.getStops(stopIds)
    }

    override fun loadMainLines(): Flow<List<Line>> {
        return stopsDao.getMainLinesByType(LineType.TRAM)
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