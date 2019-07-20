package com.jorkoh.transportezaragozakt.services.tram_api

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import com.jorkoh.transportezaragozakt.services.tram_api.responses.TramStopTramAPIResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Unofficial API service behind the shiny website for Zaragoza's tram, the site itself looks great but the endpoints
 * are a mess. It obviously doesn't use the same stop ids than the other services because that would make too much sense
 * and requests are mostly random parameter chaining. Luckily for our use case most parameters can be safely removed.
 *
 * It's currently being used as a backup when the official API fails. It would probably be more effective as
 * the main source considering the state of the official API but it would be bad manners.
 */
interface TramAPIService {
    companion object {
        const val BASE_URL = "https://www.tranviasdezaragoza.es/es/tranvia-info/"
    }

    @GET("get-part-hours?realtime=1")
    fun getTramStopTramAPI(@Query("currentStop") id: String): LiveData<ApiResponse<TramStopTramAPIResponse>>
}

fun String.officialAPIToTramAPIId() = officialToTramIds[this] ?: ""

fun Pair<String, String>.tramAPIToOfficialAPIId() = tramToOfficialIds[this] ?: ""

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

val tramToOfficialIds = mapOf(
    Pair("Mago de Oz", "Parque Goya") to "2502",
    Pair("Un Americano en París", "Parque Goya") to "2422",
    Pair("La Ventana Indiscreta", "Parque Goya") to "2322",
    Pair("Los Olvidados", "Parque Goya") to "2102",
    Pair("Argualas", "Parque Goya") to "2002",
    Pair("Casablanca", "Parque Goya") to "1902",
    Pair("Romareda", "Parque Goya") to "1802",
    Pair("Emperador Carlos V", "Parque Goya") to "1702",
    Pair("Plaza de San Francisco", "Parque Goya") to "1602",
    Pair("Fernando el Católico", "Parque Goya") to "1502",
    Pair("Gran Vía", "Parque Goya") to "1402",
    Pair("Plaza Aragón", "Parque Goya") to "1312",
    Pair("Plaza de España", "Parque Goya") to "1302",
    Pair("César Augusto", "Parque Goya") to "1202",
    Pair("Plaza del Pilar-Murallas", "Parque Goya") to "1102",
    Pair("La Chimenea", "Parque Goya") to "1002",
    Pair("María Montessori", "Parque Goya") to "902",
    Pair("León Felipe", "Parque Goya") to "802",
    Pair("Pablo Neruda", "Parque Goya") to "702",
    Pair("Adolfo Aznar", "Parque Goya") to "602",
    Pair("García Abril", "Parque Goya") to "502",
    Pair("Campus Río Ebro", "Parque Goya") to "402",
    Pair("Juslibol", "Parque Goya") to "302",
    Pair("Parque Goya", "Parque Goya") to "202",
    Pair("Avenida de la Academia", "Valdespartera") to "101",
    Pair("Parque Goya", "Valdespartera") to "201",
    Pair("Juslibol", "Valdespartera") to "301",
    Pair("Campus Río Ebro", "Valdespartera") to "401",
    Pair("Margarita Xirgu", "Valdespartera") to "501",
    Pair("Legaz Lacambra", "Valdespartera") to "601",
    Pair("Clara Campoamor", "Valdespartera") to "701",
    Pair("Rosalía de Castro", "Valdespartera") to "801",
    Pair("Martínez Soria", "Valdespartera") to "901",
    Pair("La Chimenea", "Valdespartera") to "1001",
    Pair("Plaza del Pilar-Murallas", "Valdespartera") to "1101",
    Pair("César Augusto", "Valdespartera") to "1201",
    Pair("Plaza de España", "Valdespartera") to "1301",
    Pair("Plaza Aragón", "Valdespartera") to "1311",
    Pair("Gran Vía", "Valdespartera") to "1401",
    Pair("Fernando el Católico", "Valdespartera") to "1501",
    Pair("Plaza de San Francisco", "Valdespartera") to "1601",
    Pair("Emperador Carlos V", "Valdespartera") to "1701",
    Pair("Romareda", "Valdespartera") to "1801",
    Pair("Casablanca", "Valdespartera") to "1901",
    Pair("Argualas", "Valdespartera") to "2001",
    Pair("Los Olvidados", "Valdespartera") to "2101",
    Pair("Los Pájaros", "Valdespartera") to "2301",
    Pair("Cantando bajo la Lluvia", "Valdespartera") to "2401"
)