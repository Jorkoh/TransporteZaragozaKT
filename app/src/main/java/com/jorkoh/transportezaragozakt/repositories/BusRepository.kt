package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.util.NetworkBoundResourceWithBackup
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.services.api.APIService
import com.jorkoh.transportezaragozakt.services.api.responses.bus.bus_stop.BusStopAPIResponse
import com.jorkoh.transportezaragozakt.services.web.responses.BusStopWebResponse

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
    private val apiService: APIService,
    private val stopsDao: StopsDao,
    private val db: AppDatabase
) : BusRepository {

    override fun loadStopDestinations(busStopId: String): LiveData<Resource<List<StopDestination>>> {
        return object : NetworkBoundResourceWithBackup<List<StopDestination>, BusStopAPIResponse, BusStopWebResponse>(appExecutors) {
            override fun savePrimaryCallResult(item: BusStopAPIResponse) {
                db.runInTransaction {
                    stopsDao.deleteStopDestinations(busStopId)
                    stopsDao.insertStopDestinations(item.toStopDestinations())
                }
            }

            override fun saveSecondaryCallResult(item: BusStopWebResponse) {
                db.runInTransaction {
                    stopsDao.deleteStopDestinations(busStopId)
                    stopsDao.insertStopDestinations(item.toStopDestinations())
                }
            }

            override fun shouldFetch(data: List<StopDestination>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(APIService.FRESH_TIMEOUT_BUS))
            }

            override fun loadFromDb(): LiveData<List<StopDestination>> = stopsDao.getStopDestinations(busStopId)

            override fun createPrimaryCall() = apiService.getBusStopAPI(busStopId)

            // Not even the ids on this API are consistent between the different services ¯\_(ツ)_/¯
            override fun createSecondaryCall() = apiService.getBusStopWeb(busStopId.split("-")[1])
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