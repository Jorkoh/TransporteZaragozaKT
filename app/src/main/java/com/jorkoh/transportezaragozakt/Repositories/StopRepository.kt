package com.jorkoh.transportezaragozakt.Repositories

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.Models.BusStopModel
import com.jorkoh.transportezaragozakt.Services.API.APIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


interface StopRepository{
    fun getStop(busStopId:String): LiveData<BusStopModel>
}

class StopRepositoryImplementation(val apiService: APIService) : StopRepository {

    override fun getStop(busStopId: String): LiveData<BusStopModel> {
        // This isn't an optimal implementation. We'll fix it later.
        val data = MutableLiveData<BusStopModel>()

        // Service already injected by DI thanks to Koin
        apiService.getBusStop(busStopId).enqueue(object : Callback<BusStopModel>{
            override fun onResponse(call: Call<BusStopModel>, response: Response<BusStopModel>) {
                data.value = response.body()
            }

            override fun onFailure(call: Call<BusStopModel>, t: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
        return data
    }
}