package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.util.NetworkBoundResourceWithBackup
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.services.bus_web.BusWebService
import com.jorkoh.transportezaragozakt.services.bus_web.officialAPIToBusWebId
import com.jorkoh.transportezaragozakt.services.bus_web.responses.BusStopBusWebResponse
import com.jorkoh.transportezaragozakt.services.official_api.OfficialAPIService
import com.jorkoh.transportezaragozakt.services.official_api.responses.bus.BusStopOfficialAPIResponse

interface BusRepository {
    fun loadStopDestinations(busStopId: String): LiveData<Resource<List<StopDestination>>>
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
    private val stopsDao: StopsDao,
    private val db: AppDatabase
) : BusRepository {

    override fun loadStopDestinations(busStopId: String): LiveData<Resource<List<StopDestination>>> {
        return object : NetworkBoundResourceWithBackup<List<StopDestination>, BusStopOfficialAPIResponse, BusStopBusWebResponse>(appExecutors) {
            override fun savePrimaryCallResult(item: BusStopOfficialAPIResponse) {
                db.runInTransaction {
                    stopsDao.deleteStopDestinations(busStopId)
                    stopsDao.insertStopDestinations(item.toStopDestinations())
                }
            }

            override fun saveSecondaryCallResult(item: BusStopBusWebResponse) {
                db.runInTransaction {
                    stopsDao.deleteStopDestinations(busStopId)
                    stopsDao.insertStopDestinations(item.toStopDestinations())
                }
            }

            override fun shouldFetch(data: List<StopDestination>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(OfficialAPIService.FRESH_TIMEOUT_OFFICIAL_API))
            }

            override fun loadFromDb(): LiveData<List<StopDestination>> = stopsDao.getStopDestinations(busStopId)

            override fun createPrimaryCall() = officialApiService.getBusStopOfficialAPI(busStopId)

            // Ids unfortunately are not consistent between API calls on different services
            override fun createSecondaryCall() = busWebService.getBusStopBusWeb(busStopId.officialAPIToBusWebId())
        }.asLiveData()
    }

    override fun loadStops(): LiveData<List<Stop>> {
        return stopsDao.getStopsByType(StopType.BUS)
    }

    override fun loadStops(stopIds : List<String>): LiveData<List<Stop>> {
        return stopsDao.getStops(stopIds)
    }

    override fun loadLines(): LiveData<List<Line>> {
        return stopsDao.getLinesByType(LineType.BUS)
    }

    override fun loadLineLocations(lineId : String): LiveData<List<LineLocation>> {
        return stopsDao.getLineLocations(lineId)
    }

    override fun loadLine(lineId : String) : LiveData<Line>{
        return stopsDao.getLine(lineId)
    }
}