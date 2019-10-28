package com.jorkoh.transportezaragozakt.repositories

import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.db.daos.StopsDao
import com.jorkoh.transportezaragozakt.repositories.util.NetworkBoundResourceWithBackup
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.services.bus_web.BusWebService
import com.jorkoh.transportezaragozakt.services.bus_web.officialAPIToBusWebId
import com.jorkoh.transportezaragozakt.services.bus_web.responses.bus.BusStopBusWebResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiSuccessResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.CtazAPIService
import com.jorkoh.transportezaragozakt.services.ctaz_api.officialAPIToCtazAPIId
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.bus.BusStopCtazAPIResponse
import com.jorkoh.transportezaragozakt.services.official_api.OfficialAPIService
import com.jorkoh.transportezaragozakt.services.official_api.responses.bus.BusStopOfficialAPIResponse
import kotlinx.coroutines.flow.Flow

interface BusRepository {
    fun loadStop(busStopId: String): Flow<Stop>
    fun loadStopDestinations(busStopId: String): Flow<Resource<List<StopDestination>>>
    fun loadStops(): Flow<List<Stop>>
    fun loadStops(stopIds: List<String>): Flow<List<Stop>>
    fun loadMainLines(): Flow<List<Line>>
    fun loadLineLocations(lineId: String): Flow<List<LineLocation>>
    fun loadLine(lineId: String): Flow<Line>
    fun loadAlternativeLineIds(lineId: String): Flow<List<String>>
}

class BusRepositoryImplementation(
    private val officialApiService: OfficialAPIService,
    private val busWebService: BusWebService,
    private val ctazAPIService: CtazAPIService,
    private val stopsDao: StopsDao
) : BusRepository {

    companion object {
        //Just to limit users a bit from spamming requests
        const val FRESH_TIMEOUT = 10
    }

    override fun loadStopDestinations(busStopId: String): Flow<Resource<List<StopDestination>>> {
        return object :
            NetworkBoundResourceWithBackup<List<StopDestination>, BusStopOfficialAPIResponse, BusStopBusWebResponse, BusStopCtazAPIResponse>() {
            override fun processPrimaryResponse(response: ApiSuccessResponse<BusStopOfficialAPIResponse>): List<StopDestination> {
                return response.body.toStopDestinations(busStopId)
            }

            override fun processSecondaryResponse(response: ApiSuccessResponse<BusStopBusWebResponse>): List<StopDestination> {
                return response.body.toStopDestinations(busStopId)
            }

            override fun processTertiaryResponse(response: ApiSuccessResponse<BusStopCtazAPIResponse>): List<StopDestination> {
                return response.body.toStopDestinations(busStopId)
            }

            override suspend fun saveCallResult(result: List<StopDestination>) {
                stopsDao.replaceStopDestinations(busStopId, result)
            }

            override fun shouldFetch(data: List<StopDestination>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(FRESH_TIMEOUT))
            }

            override suspend fun loadFromDb() = stopsDao.getStopDestinations(busStopId)

            override suspend fun fetchPrimarySource() = officialApiService.getBusStopOfficialAPI(busStopId)

            override suspend fun fetchSecondarySource() = busWebService.getBusStopBusWeb(busStopId.officialAPIToBusWebId())

            override suspend fun fetchTertiarySource() = ctazAPIService.getBusStopCtazAPI(busStopId.officialAPIToCtazAPIId())
        }.asFlow()
    }

    override fun loadStop(busStopId: String): Flow<Stop> {
        return stopsDao.getStop(busStopId)
    }

    override fun loadStops(): Flow<List<Stop>> {
        return stopsDao.getStopsByType(StopType.BUS)
    }

    override fun loadStops(stopIds: List<String>): Flow<List<Stop>> {
        return stopsDao.getStops(stopIds)
    }

    override fun loadMainLines(): Flow<List<Line>> {
        return stopsDao.getMainLinesByType(LineType.BUS)
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