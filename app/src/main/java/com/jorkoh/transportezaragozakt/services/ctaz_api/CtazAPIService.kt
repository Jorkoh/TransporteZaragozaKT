package com.jorkoh.transportezaragozakt.services.ctaz_api

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.CtazAPIService.Companion.BASE_URL
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.bus.BusStopCtazAPIResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.tram.TramStopCtazAPIResponse
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Unofficial API service behind CTAZ website, barely anyone know that this website provides bus and tram arrival times.
 * It's not a real API since it doesn't have actual endpoints, the JSON responses are malformed and the content type is clear text
 * but it calls itself an API. Pretty sure that the php backend just takes the URL and splits it by the questions marks.
 *
 * Pay attention to the OkHttp interceptor added to this service, it cleans up the response into properly formatted JSON. It's currently
 * being used as a backup when the official API and the official websites fail.
 */
interface CtazAPIService {
    companion object {
        const val BASE_URL = "http://api.consorciozaragoza.es/"
    }

    @GET
    fun getBusStopCtazAPI(@Url url: String): LiveData<ApiResponse<BusStopCtazAPIResponse>>

    @GET
    fun getTramStopCtazAPI(@Url url: String): LiveData<ApiResponse<TramStopCtazAPIResponse>>

}

// This kind of URLs are impossible to form with Retrofit for obvious reasons
fun String.stopIdToCtazAPIBusStopUrl() = "$BASE_URL???urban_arrival_time?${this.split("-")[1]}"

fun String.stopIdToCtazAPITramStopUrl() = "$BASE_URL???tram_time?$this"

fun String.fixLine() =
    when (this) {
        "CI1" -> "Ci1"
        "CI2" -> "Ci2"
        "N01" -> "N1"
        "N02" -> "N2"
        "N03" -> "N3"
        "N04" -> "N4"
        "N05" -> "N5"
        "N06" -> "N6"
        "N07" -> "N7"
        else -> this
    }