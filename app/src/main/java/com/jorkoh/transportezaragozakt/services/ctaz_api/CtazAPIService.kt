package com.jorkoh.transportezaragozakt.services.ctaz_api

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.bus.BusStopCtazAPIResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.rural.RuralStopCtazAPIResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.rural.RuralTrackingsCtazAPIResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.tram.TramStopCtazAPIResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface CtazAPIService {
    companion object {
        const val BASE_URL = "http://api.consorciozaragoza.es/_/_/"
    }

    @Headers("Accept: application/json")
    @GET("urban_arrival_time/{stopId}")
    fun getBusStopCtazAPI(@Path("stopId") id: String): LiveData<ApiResponse<BusStopCtazAPIResponse>>

    @Headers("Accept: application/json")
    @GET("tram_time/{stopId}")
    fun getTramStopCtazAPI(@Path("stopId") id: String): LiveData<ApiResponse<TramStopCtazAPIResponse>>

    @Headers("Accept: application/json")
    @GET("arrival_time/{stopId}")
    fun getRuralStopCtazAPI(@Path("stopId") id: String): LiveData<ApiResponse<RuralStopCtazAPIResponse>>

    @Headers("Accept: application/json")
    @GET("sae")
    fun getRuralTrackings(): LiveData<ApiResponse<RuralTrackingsCtazAPIResponse>>
}

fun String.officialAPIToCtazAPIId() = this.split("-")[1]

fun String.fixLine() =
    when (val trimmedLine = this.trim()) {
        "CI1" -> "Ci1"
        "CI2" -> "Ci2"
        "N01" -> "N1"
        "N02" -> "N2"
        "N03" -> "N3"
        "N04" -> "N4"
        "N05" -> "N5"
        "N06" -> "N6"
        "N07" -> "N7"
        else -> trimmedLine
    }