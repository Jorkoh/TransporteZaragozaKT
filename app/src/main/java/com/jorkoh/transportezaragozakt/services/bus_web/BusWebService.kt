package com.jorkoh.transportezaragozakt.services.bus_web

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.services.bus_web.responses.BusStopBusWebResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BusWebService {
    companion object {
        const val BASE_URL = "http://zaragoza.avanzagrupo.com/"
    }

    @GET("frm_esquemaparadatime.php")
    fun getBusStopBusWeb(@Query("poste") id: String): LiveData<ApiResponse<BusStopBusWebResponse>>
}

fun String.officialAPIToBusWebId() = this.split("-")[1]

fun String.fixLine(): String =
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
