package com.jorkoh.transportezaragozakt.services.api

import com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStop.BusStopResponse
import com.jorkoh.transportezaragozakt.services.api.models.Bus.BusStopLocations.BusStopLocationsResponse
import com.jorkoh.transportezaragozakt.services.api.models.Tram.TramStop.TramStopResponse
import com.jorkoh.transportezaragozakt.services.api.models.Tram.TramStopLocations.TramStopLocationsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface APIService{
    companion object {
        const val BASE_URL = "https://www.zaragoza.es/sede/servicio/urbanismo-infraestructuras/transporte-urbano/"

        //Internal API values update every minute but request distribute stale data for up to 30 seconds afterwards
        const val FRESH_TIMEOUT_BUS = 70
        const val FRESH_TIMEOUT_TRAM = 70
    }

    @Headers("Accept: application/geo+json")
    @GET("poste-autobus/{id}")
    fun getBusStop(@Path("id") id: String): Call<BusStopResponse>

    @Headers("Accept: application/geo+json")
    @GET("poste-autobus?removeproperties")
    fun getBusStopsLocations() : Call<BusStopLocationsResponse>

    @Headers("Accept: application/geo+json")
    @GET("parada-tranvia/{id}")
    fun getTramStop(@Path("id") id: String): Call<TramStopResponse>

    @Headers("Accept: application/geo+json")
    @GET("parada-tranvia?removeproperties")
    fun getTramStopsLocations() : Call<TramStopLocationsResponse>
}