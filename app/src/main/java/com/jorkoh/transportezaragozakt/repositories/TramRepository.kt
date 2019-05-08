package com.jorkoh.transportezaragozakt.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.util.NetworkBoundResource
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.services.api.APIService
import com.jorkoh.transportezaragozakt.services.api.ApiResponse
import com.jorkoh.transportezaragozakt.services.api.responses.Tram.TramStop.TramStopResponse
import com.jorkoh.transportezaragozakt.services.api.responses.Tram.TramStop.toStopDestinations
import com.jorkoh.transportezaragozakt.services.api.responses.Tram.TramStopLocations.TramStopLocationsResponse
import com.jorkoh.transportezaragozakt.services.api.responses.Tram.TramStopLocations.toStops


interface TramRepository {
    fun loadStopDestinations(tramStopId: String): LiveData<Resource<List<StopDestination>>>
    fun loadStopLocations(): LiveData<Resource<List<Stop>>>
}

class TramRepositoryImplementation(
    private val appExecutors: AppExecutors,
    private val apiService: APIService,
    private val stopsDao: StopsDao,
    private val db : AppDatabase,
    private val context: Context
) : TramRepository {

    override fun loadStopDestinations(tramStopId: String): LiveData<Resource<List<StopDestination>>> {
        return object : NetworkBoundResource<List<StopDestination>, TramStopResponse>(appExecutors) {
            override fun saveCallResult(item: TramStopResponse) {
                db.runInTransaction{
                    stopsDao.deleteStopDestinations(tramStopId)
                    stopsDao.insertStopDestinations(item.toStopDestinations(context))
                }
            }

            override fun shouldFetch(data: List<StopDestination>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(APIService.FRESH_TIMEOUT_TRAM))
            }

            override fun loadFromDb(): LiveData<List<StopDestination>> = stopsDao.getStopDestinations(tramStopId)

            override fun createCall(): LiveData<ApiResponse<TramStopResponse>> = apiService.getTramStop(tramStopId)
        }.asLiveData()
    }

    override fun loadStopLocations(): LiveData<Resource<List<Stop>>> {
        return object : NetworkBoundResource<List<Stop>, TramStopLocationsResponse>(appExecutors) {
            override fun saveCallResult(item: TramStopLocationsResponse) {
                stopsDao.insertStops(item.toStops())
            }

            override fun shouldFetch(data: List<Stop>?): Boolean {
                return false
            }

            override fun loadFromDb(): LiveData<List<Stop>> = stopsDao.getStopsByType(StopType.TRAM)

            override fun createCall(): LiveData<ApiResponse<TramStopLocationsResponse>> =
                apiService.getTramStopsLocations()
        }.asLiveData()
    }
}