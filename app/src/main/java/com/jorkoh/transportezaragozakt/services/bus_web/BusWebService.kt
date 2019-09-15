package com.jorkoh.transportezaragozakt.services.bus_web

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.services.bus_web.responses.BusStopBusWebResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Scrapping service for Zaragoza Avanza bus website, it provides arrival times for the urban bus. While
 * the site looks dated and the information is relayed by an antique PHP backend serving simple cooked HTML
 * it still manages to be more reliable than the official API.
 *
 * It's currently being used as a backup when the official API fails. It would probably be more effective as
 * the main source considering the state of the official API but it would be bad manners.
 */
interface BusWebService {
    companion object {
        const val BASE_URL = "http://zaragoza.avanzagrupo.com/"
    }

    @GET("frm_esquemaparadatime.php")
    fun getBusStopBusWeb(@Query("poste") id: String): LiveData<ApiResponse<BusStopBusWebResponse>>
}

fun String.officialAPIToBusWebId() = this.split("-")[1]

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