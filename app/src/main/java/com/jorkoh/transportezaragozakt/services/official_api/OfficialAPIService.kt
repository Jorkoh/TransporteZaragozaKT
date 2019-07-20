package com.jorkoh.transportezaragozakt.services.official_api

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import com.jorkoh.transportezaragozakt.services.official_api.responses.bus.BusStopOfficialAPIResponse
import com.jorkoh.transportezaragozakt.services.official_api.responses.tram.TramStopOfficialAPIResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * Official API service provided by the city council of Zaragoza, one of its sections is dedicated to
 * Zaragoza's urban public transport system. Unfortunately it's barely maintained, endpoints are poorly thought out,
 * full of inconsistencies, useless response status codes, limited refresh rate and breaks down at random.
 *
 * In theory it also provides endpoints for line and stop locations, routes and more. In practice those are unusable.
 */
interface OfficialAPIService {
    companion object {
        const val BASE_URL = "https://www.zaragoza.es/sede/servicio/urbanismo-infraestructuras/transporte-urbano/"

        //Just to limit users a bit from spamming requests
        const val FRESH_TIMEOUT_OFFICIAL_API = 10
    }

    @Headers("Accept: application/json")
    @GET("poste-autobus/{stopId}")
    fun getBusStopOfficialAPI(@Path("stopId") id: String): LiveData<ApiResponse<BusStopOfficialAPIResponse>>

    @Headers("Accept: application/json")
    @GET("parada-tranvia/{stopId}")
    fun getTramStopOfficialAPI(@Path("stopId") id: String): LiveData<ApiResponse<TramStopOfficialAPIResponse>>
}

fun String.fixLine() =
    when (this) {
        "CI1" -> "Ci1"
        "CI2" -> "Ci2"
        else -> this
    }