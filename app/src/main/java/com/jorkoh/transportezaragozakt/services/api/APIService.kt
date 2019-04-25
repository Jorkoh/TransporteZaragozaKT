package com.jorkoh.transportezaragozakt.services.api

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.services.api.responses.Bus.BusStop.BusStopResponse
import com.jorkoh.transportezaragozakt.services.api.responses.Bus.BusStopLocations.BusStopLocationsResponse
import com.jorkoh.transportezaragozakt.services.api.responses.Tram.TramStop.TramStopResponse
import com.jorkoh.transportezaragozakt.services.api.responses.Tram.TramStopLocations.TramStopLocationsResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface APIService{
    companion object {
        const val BASE_URL = "https://www.zaragoza.es/sede/servicio/urbanismo-infraestructuras/transporte-urbano/"

        //Just to limit users a bit from spamming requests
        const val FRESH_TIMEOUT_BUS = 10
        const val FRESH_TIMEOUT_TRAM = 10
    }

    @Headers("Accept: application/geo+json")
    @GET("poste-autobus/{stopId}")
    fun getBusStop(@Path("stopId") id: String): LiveData<ApiResponse<BusStopResponse>>

    @Headers("Accept: application/geo+json")
    @GET("poste-autobus?removeproperties")
    fun getBusStopsLocations() : LiveData<ApiResponse<BusStopLocationsResponse>>

    @Headers("Accept: application/geo+json")
    @GET("parada-tranvia/{stopId}")
    fun getTramStop(@Path("stopId") id: String): LiveData<ApiResponse<TramStopResponse>>

    @Headers("Accept: application/geo+json")
    @GET("parada-tranvia?removeproperties")
    fun getTramStopsLocations() : LiveData<ApiResponse<TramStopLocationsResponse>>
}