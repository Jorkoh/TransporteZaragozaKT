package com.jorkoh.transportezaragozakt.services.official_api

import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import com.jorkoh.transportezaragozakt.services.official_api.responses.bus.BusStopOfficialAPIResponse
import com.jorkoh.transportezaragozakt.services.official_api.responses.tram.TramStopOfficialAPIResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface OfficialAPIService {
    companion object {
        const val BASE_URL = "https://www.zaragoza.es/sede/servicio/urbanismo-infraestructuras/transporte-urbano/"
    }

    @Headers("Accept: application/json")
    @GET("poste-autobus/{stopId}")
    suspend fun getBusStopOfficialAPI(@Path("stopId") id: String): ApiResponse<BusStopOfficialAPIResponse>

    @Headers("Accept: application/json")
    @GET("parada-tranvia/{stopId}")
    suspend fun getTramStopOfficialAPI(@Path("stopId") id: String): ApiResponse<TramStopOfficialAPIResponse>
}

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
        "ES1" -> "V1"
        "ES4" -> "V4"
        else -> trimmedLine
    }