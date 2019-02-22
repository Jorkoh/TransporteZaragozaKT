package com.jorkoh.transportezaragozakt.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopsDao
import com.jorkoh.transportezaragozakt.services.api.APIService
import com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStop.BusStopModel
import com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStop.toStop
import com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStop.toStopDestinations
import com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStopLocations.BusStopLocationsModel
import com.jorkoh.transportezaragozakt.services.api.models.IStop
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


interface BusRepository {
    fun getStopInfo(busStopId: String): LiveData<Stop>
    fun getStopLocations(): LiveData<BusStopLocationsModel>
}

class BusRepositoryImplementation(
    private val apiService: APIService,
    private val stopsDao: StopsDao,
    private val busStopCache: MutableMap<String, LiveData<IStop>> = mutableMapOf(),
    private var busStopLocationsCache: LiveData<BusStopLocationsModel>? = null
) : BusRepository {

    companion object {
        const val FRESH_TIMEOUT = 30
    }

    override fun getStopInfo(busStopId: String): LiveData<Stop> {
        refreshStop(busStopId)
        return stopsDao.getStop(busStopId)
    }

    fun refreshStop(busStopId: String) {
        //TODO ROOM: COROUTINE THIS CALL OR USE SOME KIND OF EXECUTOR
        if (!stopsDao.stopHasFreshInfo(busStopId, FRESH_TIMEOUT)) {
            apiService.getBusStop(busStopId).enqueue(object : Callback<BusStopModel> {
                override fun onResponse(call: Call<BusStopModel>, response: Response<BusStopModel>) {
                    checkNotNull(response.body())
                    val body = response.body()
                    if(body is BusStopModel){
                        stopsDao.insertStopDestinations(body.toStopDestinations())
                    }
                }

                override fun onFailure(call: Call<BusStopModel>, t: Throwable) {
                    //TODO: Implement this
                }
            })
            Log.d("TestingStuff", "Stop info refreshed with retrofit")
        } else {
            Log.d("TestingStuff", "Stop info is still fresh")
        }
    }

    override fun getStopLocations(): LiveData<BusStopLocationsModel> {
        val cached = busStopLocationsCache
        if (cached != null) {
            Log.d("TestingStuff", "Bus stop repository returns cached stop locations")
            return cached
        }

        val data = MutableLiveData<BusStopLocationsModel>()
        busStopLocationsCache = data

        // Service already injected by DI thanks to Koin
        apiService.getBusStopsLocations().enqueue(object : Callback<BusStopLocationsModel> {
            override fun onResponse(call: Call<BusStopLocationsModel>, response: Response<BusStopLocationsModel>) {
                data.value = response.body()
            }

            override fun onFailure(call: Call<BusStopLocationsModel>, t: Throwable) {
                //TODO: Implement this
            }
        })

        Log.d("TestingStuff", "Bus stop repository returns retrofit stop locations")
        return data
    }
}