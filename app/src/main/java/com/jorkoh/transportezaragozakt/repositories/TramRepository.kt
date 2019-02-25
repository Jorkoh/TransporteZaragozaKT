package com.jorkoh.transportezaragozakt.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.db.StopsDao
import com.jorkoh.transportezaragozakt.services.api.APIService
import com.jorkoh.transportezaragozakt.services.api.models.StopType
import com.jorkoh.transportezaragozakt.services.api.models.Tram.TramStop.TramStopResponse
import com.jorkoh.transportezaragozakt.services.api.models.Tram.TramStop.toStopDestinations
import com.jorkoh.transportezaragozakt.services.api.models.Tram.TramStopLocations.TramStopLocationsResponse
import com.jorkoh.transportezaragozakt.services.api.models.Tram.TramStopLocations.toStops
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


interface TramRepository {
    fun getStopDestinations(tramStopId: String): LiveData<List<StopDestination>>
    fun getStopLocations(): LiveData<List<Stop>>
}

class TramRepositoryImplementation(
    private val apiService: APIService,
    private val stopsDao: StopsDao
) : TramRepository {

    companion object {
        const val FRESH_TIMEOUT = 120
    }

    override fun getStopDestinations(tramStopId: String): LiveData<List<StopDestination>> {
        refreshStopDestinations(tramStopId)
        return stopsDao.getStopDestinations(tramStopId)
    }

    private fun refreshStopDestinations(tramStopId: String) {
        //TODO: Clean this, way too nested
        GlobalScope.launch {
            if (!stopsDao.stopHasFreshInfo(tramStopId, FRESH_TIMEOUT)) {
                fetchStop(tramStopId)
            } else {
                Log.d("TestingStuff", "Tram Stop info is still fresh")
            }
        }
    }

    private fun fetchStop(tramStopId: String){
        apiService.getTramStop(tramStopId).enqueue(object : Callback<TramStopResponse> {
            override fun onResponse(call: Call<TramStopResponse>, response: Response<TramStopResponse>) {
                checkNotNull(response.body())
                val body = response.body()
                if (body is TramStopResponse) {
                    GlobalScope.launch {
                        stopsDao.insertStopDestinations(body.toStopDestinations())
                    }
                }
            }

            override fun onFailure(call: Call<TramStopResponse>, t: Throwable) {
                //TODO: Implement this
            }
        })
        Log.d("TestingStuff", "Tram Stop info refreshed with retrofit")
    }

    override fun getStopLocations(): LiveData<List<Stop>> {
        //TODO: This is iffy
        GlobalScope.launch {
            if (!stopsDao.areStopLocationsSaved(StopType.TRAM)) {
                fetchStopLocations()
            } else {
                Log.d("TestingStuff", "Tram stop locations already saved")
            }
        }
        return stopsDao.getStopsByType(StopType.TRAM)
    }

    private fun fetchStopLocations() {
        apiService.getTramStopsLocations().enqueue(object : Callback<TramStopLocationsResponse> {
            override fun onResponse(
                call: Call<TramStopLocationsResponse>,
                response: Response<TramStopLocationsResponse>
            ) {
                checkNotNull(response.body())
                val body = response.body()
                if (body is TramStopLocationsResponse) {
                    //TODO: Making stop objects just for locations feels weird
                    GlobalScope.launch {
                        stopsDao.insertStops(body.toStops())
                    }
                }
            }

            override fun onFailure(call: Call<TramStopLocationsResponse>, t: Throwable) {
                //TODO: Implement this
            }
        })
        Log.d("TestingStuff", "Tram stop locations refreshed with retrofit")
    }
}