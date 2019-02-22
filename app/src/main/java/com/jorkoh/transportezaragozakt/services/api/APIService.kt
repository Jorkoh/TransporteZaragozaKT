package com.jorkoh.transportezaragozakt.services.api

import com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStop.BusStopModel
import com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStopLocations.BusStopLocationsModel
import com.jorkoh.transportezaragozakt.services.api.models.Tram.TramStop.TramStopModel
import com.jorkoh.transportezaragozakt.services.api.models.Tram.TramStopLocations.TramStopLocationsModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface APIService{
    companion object {
        const val BASE_URL = "https://www.zaragoza.es/sede/servicio/urbanismo-infraestructuras/transporte-urbano/"
    }

    @Headers("Accept: application/geo+json")
    @GET("poste-autobus/{id}")
    fun getBusStop(@Path("id") id: String): Call<BusStopModel>

    @Headers("Accept: application/geo+json")
    @GET("poste-autobus?removeproperties")
    fun getBusStopsLocations() : Call<BusStopLocationsModel>

    @Headers("Accept: application/geo+json")
    @GET("parada-tranvia/{id}")
    fun getTramStop(@Path("id") id: String): Call<TramStopModel>

    @Headers("Accept: application/geo+json")
    @GET("parada-tranvia?removeproperties")
    fun getTramStopsLocations() : Call<TramStopLocationsModel>
}