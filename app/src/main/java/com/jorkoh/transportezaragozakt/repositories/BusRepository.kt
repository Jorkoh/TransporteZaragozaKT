package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.services.api.APIService
import com.jorkoh.transportezaragozakt.services.api.ApiResponse
import com.jorkoh.transportezaragozakt.services.api.responses.Bus.BusStop.BusStopResponse
import com.jorkoh.transportezaragozakt.services.api.responses.Bus.BusStop.toStopDestinations
import com.jorkoh.transportezaragozakt.services.api.responses.Bus.BusStopLocations.BusStopLocationsResponse
import com.jorkoh.transportezaragozakt.services.api.responses.Bus.BusStopLocations.toStops
import java.util.*

interface BusRepository {
    fun loadStopDestinations(busStopId: String): LiveData<Resource<List<StopDestination>>>
    fun loadStopLocations(): LiveData<Resource<List<Stop>>>
}

class BusRepositoryImplementation(
    private val appExecutors: AppExecutors,
    private val apiService: APIService,
    private val stopsDao: StopsDao,
    private val db: AppDatabase
) : BusRepository {

    override fun loadStopDestinations(busStopId: String): LiveData<Resource<List<StopDestination>>> {
        return object : NetworkBoundResource<List<StopDestination>, BusStopResponse>(appExecutors) {
            override fun saveCallResult(item: BusStopResponse) {
                db.runInTransaction {
                    stopsDao.deleteStopDestinations(busStopId)
                    stopsDao.insertStopDestinations(item.toStopDestinations())
                }
            }

            override fun shouldFetch(data: List<StopDestination>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(APIService.FRESH_TIMEOUT_BUS))
            }

            override fun loadFromDb(): LiveData<List<StopDestination>> = stopsDao.getStopDestinations(busStopId)

            override fun createCall(): LiveData<ApiResponse<BusStopResponse>> = apiService.getBusStop(busStopId)
        }.asLiveData()
    }

    // The list is fresh if its oldest member is fresh
    fun List<StopDestination>.isFresh(timeoutInSeconds:Int) = (this.minBy { it.updatedAt }?.isFresh(timeoutInSeconds) ?: false)

    override fun loadStopLocations(): LiveData<Resource<List<Stop>>> {
        return object : NetworkBoundResource<List<Stop>, BusStopLocationsResponse>(appExecutors) {
            override fun saveCallResult(item: BusStopLocationsResponse) {
                stopsDao.insertStops(item.toStops())
            }

            override fun shouldFetch(data: List<Stop>?): Boolean {
                return false
            }

            override fun loadFromDb(): LiveData<List<Stop>> = stopsDao.getStopsByType(StopType.BUS)

            override fun createCall(): LiveData<ApiResponse<BusStopLocationsResponse>> =
                apiService.getBusStopsLocations()
        }.asLiveData()
    }
}