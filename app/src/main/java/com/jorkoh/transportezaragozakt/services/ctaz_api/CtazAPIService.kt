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
import java.util.*

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
    suspend fun getRuralTrackings(): ApiResponse<RuralTrackingsCtazAPIResponse>
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
        "201B" -> "201"
        else -> trimmedLine
    }

// Basically CTAZ uses the same "remaining_time" field for actual real time updated remaining times and estimated
// arrival times in 24 hour format, this method transforms both into the format used by the other services
fun Date.toRemainingMinutes(isRemaining: Boolean): String {
    // Can't use java.time because API 21, won't add Joda Time dependency just for this so it will stay ugly for now
    val minutes = if (isRemaining) {
        val difference = Calendar.getInstance()
        difference.time = this
        (difference.get(Calendar.MINUTE) + difference.get(Calendar.HOUR) * 60 + if (difference.get(Calendar.SECOND) >= 30) 1 else 0)
    } else {
        val now = Date()
        (minutes - now.minutes) + (hours - now.hours) * 60 + (if ((seconds - now.seconds) * 60 >= 30) 1 else 0)
    }
    return "$minutes minutos."
}