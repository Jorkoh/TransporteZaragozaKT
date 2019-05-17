package com.jorkoh.transportezaragozakt.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.util.NetworkBoundResource
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.services.api.APIService
import com.jorkoh.transportezaragozakt.services.api.ApiResponse
import com.jorkoh.transportezaragozakt.services.api.responses.Bus.BusStop.BusStopResponse
import com.jorkoh.transportezaragozakt.services.api.responses.Bus.BusStop.toStopDestinations
import com.jorkoh.transportezaragozakt.services.api.responses.Bus.BusStopLocations.BusStopLocationsResponse
import com.jorkoh.transportezaragozakt.services.api.responses.Bus.BusStopLocations.toStops

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
    private val db: AppDatabase,
    private val context: Context
) : BusRepository {

    override fun loadStopDestinations(busStopId: String): LiveData<Resource<List<StopDestination>>> {
        return object : NetworkBoundResource<List<StopDestination>, BusStopResponse>(appExecutors) {
            override fun saveCallResult(item: BusStopResponse) {
                db.runInTransaction {
                    stopsDao.deleteStopDestinations(busStopId)
                    stopsDao.insertStopDestinations(item.toStopDestinations(context))
                }
            }

            override fun shouldFetch(data: List<StopDestination>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(APIService.FRESH_TIMEOUT_BUS))
            }

            override fun loadFromDb(): LiveData<List<StopDestination>> = stopsDao.getStopDestinations(busStopId)

            override fun createCall(): LiveData<ApiResponse<BusStopResponse>> = apiService.getBusStop(busStopId)
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