package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.util.NetworkBoundResourceWithBackup
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.services.common.util.ApiSuccessResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.CtazAPIService
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.tram.TramStopCtazAPIResponse
import com.jorkoh.transportezaragozakt.services.official_api.OfficialAPIService
import com.jorkoh.transportezaragozakt.services.official_api.responses.tram.TramStopOfficialAPIResponse
import com.jorkoh.transportezaragozakt.services.tram_api.TramAPIService
import com.jorkoh.transportezaragozakt.services.tram_api.officialAPIToTramAPIId
import com.jorkoh.transportezaragozakt.services.tram_api.responses.TramStopTramAPIResponse


interface TramRepository {
    fun loadStopDestinations(tramStopId: String): LiveData<Resource<List<StopDestination>>>
    fun loadStop(tramStopId: String): LiveData<Stop>
    fun loadStops(): LiveData<List<Stop>>
    fun loadLines(): LiveData<List<Line>>
    fun loadLineLocations(lineId: String): LiveData<List<LineLocation>>
    fun loadLine(lineId: String): LiveData<Line>
    fun loadStops(stopIds: List<String>): LiveData<List<Stop>>
}

class TramRepositoryImplementation(
    private val appExecutors: AppExecutors,
    private val officialApiService: OfficialAPIService,
    private val tramAPIService: TramAPIService,
    private val ctazAPIService: CtazAPIService,
    private val stopsDao: StopsDao,
    private val db: AppDatabase
) : TramRepository {

    companion object {
        //Just to limit users a bit from spamming requests
        const val FRESH_TIMEOUT = 10
    }

    override fun loadStopDestinations(tramStopId: String): LiveData<Resource<List<StopDestination>>> {
        return object :
            NetworkBoundResourceWithBackup<List<StopDestination>, TramStopOfficialAPIResponse, TramStopTramAPIResponse, TramStopCtazAPIResponse>(
                appExecutors
            ) {
            override fun processPrimaryResponse(response: ApiSuccessResponse<TramStopOfficialAPIResponse>): List<StopDestination> {
                return response.body.toStopDestinations(tramStopId)
            }

            override fun processSecondaryResponse(response: ApiSuccessResponse<TramStopTramAPIResponse>): List<StopDestination> {
                return response.body.toStopDestinations(tramStopId)
            }

            override fun processTertiaryResponse(response: ApiSuccessResponse<TramStopCtazAPIResponse>): List<StopDestination> {
                return response.body.toStopDestinations(tramStopId)
            }

            override fun saveCallResult(result: List<StopDestination>) {
                db.runInTransaction {
                    stopsDao.deleteStopDestinations(tramStopId)
                    stopsDao.insertStopDestinations(result)
                }
            }

            override fun shouldFetch(data: List<StopDestination>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(FRESH_TIMEOUT))
            }

            override fun loadFromDb(): LiveData<List<StopDestination>> = stopsDao.getStopDestinations(tramStopId)

            override fun createPrimaryCall() = officialApiService.getTramStopOfficialAPI(tramStopId)

            override fun createSecondaryCall() = tramAPIService.getTramStopTramAPI(tramStopId.officialAPIToTramAPIId())

            override fun createTertiaryCall() = ctazAPIService.getTramStopCtazAPI(tramStopId)
        }.asLiveData()
    }

    override fun loadStop(tramStopId: String): LiveData<Stop> {
        return stopsDao.getStop(tramStopId)
    }

    override fun loadStops(): LiveData<List<Stop>> {
        return stopsDao.getStopsByType(StopType.TRAM)
    }

    override fun loadStops(stopIds: List<String>): LiveData<List<Stop>> {
        return stopsDao.getStops(stopIds)
    }

    override fun loadLines(): LiveData<List<Line>> {
        return stopsDao.getLinesByType(LineType.TRAM)
    }

    override fun loadLineLocations(lineId: String): LiveData<List<LineLocation>> {
        return stopsDao.getLineLocations(lineId)
    }

    override fun loadLine(lineId: String): LiveData<Line> {
        return stopsDao.getLine(lineId)
    }
}