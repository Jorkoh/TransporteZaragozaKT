package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.util.NetworkBoundResourceWithBackup
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.services.bus_web.BusWebService
import com.jorkoh.transportezaragozakt.services.bus_web.officialAPIToBusWebId
import com.jorkoh.transportezaragozakt.services.bus_web.responses.BusStopBusWebResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiSuccessResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.CtazAPIService
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.bus.BusStopCtazAPIResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.stopIdToCtazAPIBusStopUrl
import com.jorkoh.transportezaragozakt.services.official_api.OfficialAPIService
import com.jorkoh.transportezaragozakt.services.official_api.responses.bus.BusStopOfficialAPIResponse

interface BusRepository {
    fun loadStopDestinations(busStopId: String): LiveData<Resource<List<StopDestination>>>
    fun loadStop(busStopId: String): LiveData<Stop>
    fun loadStops(): LiveData<List<Stop>>
    fun loadLines(): LiveData<List<Line>>
    fun loadLineLocations(lineId: String): LiveData<List<LineLocation>>
    fun loadLine(lineId: String): LiveData<Line>
    fun loadStops(stopIds: List<String>): LiveData<List<Stop>>
}

class BusRepositoryImplementation(
    private val appExecutors: AppExecutors,
    private val officialApiService: OfficialAPIService,
    private val busWebService: BusWebService,
    private val ctazAPIService: CtazAPIService,
    private val stopsDao: StopsDao,
    private val db: AppDatabase
) : BusRepository {

    override fun loadStopDestinations(busStopId: String): LiveData<Resource<List<StopDestination>>> {
        return object :
            NetworkBoundResourceWithBackup<List<StopDestination>, BusStopOfficialAPIResponse, BusStopBusWebResponse, BusStopCtazAPIResponse>(
                appExecutors
            ) {
            override fun processPrimaryResponse(response: ApiSuccessResponse<BusStopOfficialAPIResponse>): List<StopDestination> {
                return response.body.toStopDestinations(busStopId)
            }

            override fun processSecondaryResponse(response: ApiSuccessResponse<BusStopBusWebResponse>): List<StopDestination> {
                return response.body.toStopDestinations(busStopId)
            }

            override fun processTertiaryResponse(response: ApiSuccessResponse<BusStopCtazAPIResponse>): List<StopDestination> {
                return response.body.toStopDestinations(busStopId)
            }

            override fun saveCallResult(result: List<StopDestination>) {
                db.runInTransaction {
                    stopsDao.deleteStopDestinations(busStopId)
                    stopsDao.insertStopDestinations(result)
                }
            }

            override fun shouldFetch(data: List<StopDestination>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(OfficialAPIService.FRESH_TIMEOUT_OFFICIAL_API))
            }

            override fun loadFromDb(): LiveData<List<StopDestination>> = stopsDao.getStopDestinations(busStopId)

            override fun createPrimaryCall() = officialApiService.getBusStopOfficialAPI(busStopId)

            override fun createSecondaryCall() = busWebService.getBusStopBusWeb(busStopId.officialAPIToBusWebId())

            override fun createTertiaryCall() = ctazAPIService.getBusStopCtazAPI(busStopId.stopIdToCtazAPIBusStopUrl())
        }.asLiveData()
    }

    override fun loadStop(busStopId: String): LiveData<Stop> {
        return stopsDao.getStop(busStopId)
    }

    override fun loadStops(): LiveData<List<Stop>> {
        return stopsDao.getStopsByType(StopType.BUS)
    }

    override fun loadStops(stopIds: List<String>): LiveData<List<Stop>> {
        return stopsDao.getStops(stopIds)
    }

    override fun loadLines(): LiveData<List<Line>> {
        return stopsDao.getLinesByType(LineType.BUS)
    }

    override fun loadLineLocations(lineId: String): LiveData<List<LineLocation>> {
        return stopsDao.getLineLocations(lineId)
    }

    override fun loadLine(lineId: String): LiveData<Line> {
        return stopsDao.getLine(lineId)
    }
}