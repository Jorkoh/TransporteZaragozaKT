package com.jorkoh.transportezaragozakt.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.db.daos.BusDao
import com.jorkoh.transportezaragozakt.models.Bus.BusStop.BusStopModel
import com.jorkoh.transportezaragozakt.models.Bus.BusStopLocations.BusStopLocationsModel
import com.jorkoh.transportezaragozakt.models.IStop
import com.jorkoh.transportezaragozakt.services.api.APIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


interface BusRepository {
    fun getStopInfo(busStopId: String): LiveData<IStop>
    fun getStopLocations(): LiveData<BusStopLocationsModel>
}

class BusRepositoryImplementation(
    private val apiService: APIService,
    private val busDao: BusDao,
    private val busStopCache: MutableMap<String, LiveData<IStop>> = mutableMapOf(),
    private var busStopLocationsCache: LiveData<BusStopLocationsModel>? = null
) : BusRepository {

    companion object {
        const val FRESH_TIMEOUT = 30
    }

    override fun getStopInfo(busStopId: String): LiveData<IStop> {
        refreshStop(busStopId)
        return busDao.getStop(busStopId) as LiveData<IStop>

        /**/
        val cached = busStopCache[busStopId]
        if (cached != null) {
            Log.d("TestingStuff", "Bus stop repository returns cached stop info")
            return cached
        }

        val data = MutableLiveData<IStop>()
        busStopCache[busStopId] = data

        // Service already injected by DI thanks to Koin
        apiService.getBusStop(busStopId).enqueue(object : Callback<BusStopModel> {
            override fun onResponse(call: Call<BusStopModel>, response: Response<BusStopModel>) {
                data.value = response.body()
            }

            override fun onFailure(call: Call<BusStopModel>, t: Throwable) {
                //TODO: Implement this
            }
        })

        Log.d("TestingStuff", "Bus stop repository returns retrofit stop info")
        return data
    }

    fun refreshStop(busStopId: String) {
        //TODO ROOM: COROUTINE THIS CALL OR USE SOME KIND OF EXECUTOR
        if (!busDao.stopHasFreshInfo(busStopId, FRESH_TIMEOUT)) {
            apiService.getBusStop(busStopId).enqueue(object : Callback<BusStopModel> {
                override fun onResponse(call: Call<BusStopModel>, response: Response<BusStopModel>) {
                    //TODO ROOM: THIS SHOULD BE AN UPDATE/INSERT
                    busDao.insertStop(checkNotNull(response.body()))
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