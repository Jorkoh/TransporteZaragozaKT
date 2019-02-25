package com.jorkoh.transportezaragozakt.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopsDao
import com.jorkoh.transportezaragozakt.services.api.APIService
import com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStop.BusStopResponse
import com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStop.toStopDestinations
import com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStopLocations.BusStopLocationsResponse
import com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStopLocations.toStops
import com.jorkoh.transportezaragozakt.services.api.models.StopType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


interface BusRepository {
    fun getStopDestinations(busStopId: String): LiveData<List<StopDestination>>
    fun getStopLocations(): LiveData<List<Stop>>
}

class BusRepositoryImplementation(
    private val apiService: APIService,
    private val stopsDao: StopsDao
) : BusRepository {

    companion object {
        const val FRESH_TIMEOUT = 60
    }

    override fun getStopDestinations(busStopId: String): LiveData<List<StopDestination>> {
        refreshStopDestinations(busStopId)
        return stopsDao.getStopDestinations(busStopId)
    }

    private fun refreshStopDestinations(busStopId: String) {
        //TODO: Clean this, way too nested
        GlobalScope.launch {
            if (!stopsDao.stopHasFreshInfo(busStopId, FRESH_TIMEOUT)) {
                fetchStop(busStopId)
            } else {
                Log.d("TestingStuff", "Bus Stop info is still fresh")
            }
        }
    }

    private fun fetchStop(busStopId: String) {
        apiService.getBusStop(busStopId).enqueue(object : Callback<BusStopResponse> {
            override fun onResponse(call: Call<BusStopResponse>, response: Response<BusStopResponse>) {
                checkNotNull(response.body())
                val body = response.body()
                if (body is BusStopResponse) {
                    GlobalScope.launch {
                        stopsDao.insertStopDestinations(body.toStopDestinations())
                    }
                }
            }

            override fun onFailure(call: Call<BusStopResponse>, t: Throwable) {
                //TODO: Implement this
            }
        })
        Log.d("TestingStuff", "Bus Stop info refreshed with retrofit")
    }

    override fun getStopLocations(): LiveData<List<Stop>> {
        //TODO: This is iffy
        GlobalScope.launch {
            if (!stopsDao.areStopLocationsSaved(StopType.BUS)) {
                fetchStopLocations()
            } else {
                Log.d("TestingStuff", "Bus stop locations already saved")
            }
        }
        return stopsDao.getStopsByType(StopType.BUS)
    }

    private fun fetchStopLocations() {
        apiService.getBusStopsLocations().enqueue(object : Callback<BusStopLocationsResponse> {
            override fun onResponse(
                call: Call<BusStopLocationsResponse>,
                response: Response<BusStopLocationsResponse>
            ) {
                checkNotNull(response.body())
                val body = response.body()
                if (body is BusStopLocationsResponse) {
                    //TODO: Making stop objects just for locations feels weird
                    GlobalScope.launch {
                        stopsDao.insertStops(body.toStops())
                    }
                }
            }

            override fun onFailure(call: Call<BusStopLocationsResponse>, t: Throwable) {
                //TODO: Implement this
            }
        })
        Log.d("TestingStuff", "Bus stop locations refreshed with retrofit")
    }
}