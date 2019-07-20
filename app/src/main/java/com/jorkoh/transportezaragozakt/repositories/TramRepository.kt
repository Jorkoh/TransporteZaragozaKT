package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.util.NetworkBoundResourceWithBackup
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.services.official_api.OfficialAPIService
import com.jorkoh.transportezaragozakt.services.official_api.responses.tram.TramStopOfficialAPIResponse
import com.jorkoh.transportezaragozakt.services.tram_api.TramAPIService
import com.jorkoh.transportezaragozakt.services.tram_api.officialAPIToTramAPIId
import com.jorkoh.transportezaragozakt.services.tram_api.responses.TramStopTramAPIResponse


interface TramRepository {
    fun loadStopDestinations(tramStopId: String): LiveData<Resource<List<StopDestination>>>
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
    private val stopsDao: StopsDao,
    private val db: AppDatabase
) : TramRepository {

    override fun loadStopDestinations(tramStopId: String): LiveData<Resource<List<StopDestination>>> {
        return object :
            NetworkBoundResourceWithBackup<List<StopDestination>, TramStopOfficialAPIResponse, TramStopTramAPIResponse>(appExecutors) {
            override fun savePrimaryCallResult(item: TramStopOfficialAPIResponse) {
                db.runInTransaction {
                    stopsDao.deleteStopDestinations(tramStopId)
                    stopsDao.insertStopDestinations(item.toStopDestinations())
                }
            }

            override fun saveSecondaryCallResult(item: TramStopTramAPIResponse) {
                db.runInTransaction {
                    stopsDao.deleteStopDestinations(tramStopId)
                    stopsDao.insertStopDestinations(item.toStopDestinations())
                }
            }

            override fun shouldFetch(data: List<StopDestination>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(OfficialAPIService.FRESH_TIMEOUT_OFFICIAL_API))
            }

            override fun loadFromDb(): LiveData<List<StopDestination>> = stopsDao.getStopDestinations(tramStopId)

            override fun createPrimaryCall() = officialApiService.getTramStopOfficialAPI(tramStopId)

            // Ids unfortunately are not consistent between API calls on different services
            override fun createSecondaryCall() = tramAPIService.getTramStopTramAPI(tramStopId.officialAPIToTramAPIId())
        }.asLiveData()
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