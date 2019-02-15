package com.jorkoh.transportezaragozakt.Repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.Models.Bus.BusStop.BusStopModel
import com.jorkoh.transportezaragozakt.Models.Bus.BusStopLocations.BusStopLocationsModel
import com.jorkoh.transportezaragozakt.Services.API.APIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


interface BusRepository {
    fun getStopInfo(busStopId: String): LiveData<BusStopModel>
    fun getStopLocations(): LiveData<BusStopLocationsModel>
}

class BusRepositoryImplementation(
    private val apiService: APIService,
    private val busStopCache: MutableMap<String, LiveData<BusStopModel>> = mutableMapOf(),
    private var busStopLocationsCache: LiveData<BusStopLocationsModel>? = null
) : BusRepository {

    override fun getStopInfo(busStopId: String): LiveData<BusStopModel> {
        val cached = busStopCache[busStopId]
        if (cached != null) {
            Log.d("TestingStuff", "Bus stop repository returns cached stop info")
            return cached
        }

        val data = MutableLiveData<BusStopModel>()
        busStopCache[busStopId] = data

        // Service already injected by DI thanks to Koin
        apiService.getBusStop(busStopId).enqueue(object : Callback<BusStopModel> {
            override fun onResponse(call: Call<BusStopModel>, response: Response<BusStopModel>) {
                data.value = response.body()
            }

            override fun onFailure(call: Call<BusStopModel>, t: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        Log.d("TestingStuff", "Bus stop repository returns retrofit stop info")
        return data
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
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        Log.d("TestingStuff", "Bus stop repository returns retrofit stop locations")
        return data
    }
}