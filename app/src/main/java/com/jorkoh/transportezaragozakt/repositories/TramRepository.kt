package com.jorkoh.transportezaragozakt.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.services.api.models.IStop
import com.jorkoh.transportezaragozakt.services.api.models.Tram.TramStop.TramStopModel
import com.jorkoh.transportezaragozakt.services.api.models.Tram.TramStopLocations.TramStopLocationsModel
import com.jorkoh.transportezaragozakt.services.api.APIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


interface TramRepository {
    fun getStopInfo(tramStopId: String): LiveData<IStop>
    fun getStopLocations(): LiveData<TramStopLocationsModel>
}

class TramRepositoryImplementation(
    private val apiService: APIService,
    private val tramStopCache: MutableMap<String, LiveData<IStop>> = mutableMapOf(),
    private var tramStopLocationsCache: LiveData<TramStopLocationsModel>? = null
) : TramRepository {

    override fun getStopInfo(tramStopId: String): LiveData<IStop> {
        val cached = tramStopCache[tramStopId]
        if (cached != null) {
            Log.d("TestingStuff", "Tram stop repository returns cached stop info")
            return cached
        }

        val data = MutableLiveData<IStop>()
        tramStopCache[tramStopId] = data

        // Service already injected by DI thanks to Koin
        apiService.getTramStop(tramStopId).enqueue(object : Callback<TramStopModel> {
            override fun onResponse(call: Call<TramStopModel>, response: Response<TramStopModel>) {
                data.value = response.body()
            }

            override fun onFailure(call: Call<TramStopModel>, t: Throwable) {
                //TODO: Implement this
            }
        })

        Log.d("TestingStuff", "Tram stop repository returns retrofit stop info")
        return data
    }

    override fun getStopLocations(): LiveData<TramStopLocationsModel> {
        val cached = tramStopLocationsCache
        if (cached != null) {
            Log.d("TestingStuff", "Tram stop repository returns cached stop locations")
            return cached
        }

        val data = MutableLiveData<TramStopLocationsModel>()
        tramStopLocationsCache = data

        // Service already injected by DI thanks to Koin
        apiService.getTramStopsLocations().enqueue(object : Callback<TramStopLocationsModel> {
            override fun onResponse(call: Call<TramStopLocationsModel>, response: Response<TramStopLocationsModel>) {
                data.value = response.body()
            }

            override fun onFailure(call: Call<TramStopLocationsModel>, t: Throwable) {
                //TODO: Implement this
            }
        })

        Log.d("TestingStuff", "Tram stop repository returns retrofit stop locations")
        return data
    }
}