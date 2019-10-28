package com.jorkoh.transportezaragozakt.services.tram_api

import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import com.jorkoh.transportezaragozakt.services.tram_api.responses.tram.TramStopTramAPIResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TramAPIService {
    companion object {
        const val BASE_URL = "https://www.tranviasdezaragoza.es/es/tranvia-info/"
    }

    @GET("get-part-hours?realtime=1")
    suspend fun getTramStopTramAPI(@Query("currentStop") id: String): ApiResponse<TramStopTramAPIResponse>
}

fun String.officialAPIToTramAPIId() = officialToTramIds[this] ?: ""

val officialToTramIds = mapOf(
    "2502" to "1",
    "2422" to "2",
    "2322" to "3",
    "2102" to "4",
    "2002" to "6",
    "1902" to "7",
    "1802" to "8",
    "1702" to "9",
    "1602" to "10",
    "1502" to "11",
    "1402" to "12",
    "1312" to "13",
    "1302" to "14",
    "1202" to "15",
    "1102" to "16",
    "1002" to "17",
    "902" to "18",
    "802" to "19",
    "702" to "20",
    "602" to "21",
    "502" to "22",
    "402" to "23",
    "302" to "25",
    "202" to "26",
    "101" to "28",
    "201" to "29",
    "301" to "30",
    "401" to "32",
    "501" to "33",
    "601" to "34",
    "701" to "35",
    "801" to "36",
    "901" to "37",
    "1001" to "38",
    "1101" to "39",
    "1201" to "40",
    "1301" to "41",
    "1311" to "42",
    "1401" to "43",
    "1501" to "44",
    "1601" to "45",
    "1701" to "46",
    "1801" to "47",
    "1901" to "48",
    "2001" to "49",
    "2101" to "51",
    "2301" to "52",
    "2401" to "53"
)