package com.jorkoh.transportezaragozakt.services.ctaz_api.responses.rural

import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.services.common.responses.RuralTrackingsResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class RuralTrackingsCtazAPIResponse(
    @Json(name= "sae")
    val ruralTrackingsResponse: List<RuralTrackingResponse>
) : RuralTrackingsResponse{

    override fun toRuralTrackings(): List<RuralTracking> {
        val ruralTrackings = mutableListOf<RuralTracking>()
        ruralTrackingsResponse
            .distinctBy { it.vehicleId }
            .forEach { ruralTrackingResponse ->
                ruralTrackings += RuralTracking(
                    ruralTrackingResponse.vehicleId,
                    ruralTrackingResponse.line,
                    LatLng(ruralTrackingResponse.latitude, ruralTrackingResponse.longitude),
                    Date()
                )
            }
        return ruralTrackings
    }
}

@JsonClass(generateAdapter = true)
data class RuralTrackingResponse(
    @Json(name = "bus")
    val vehicleId: String,

    @Json(name = "linea")
    val line: String,

    @Json(name = "nombre_linea")
    val lineName: String,

    @Json(name = "latitud")
    val latitude: Double,

    @Json(name = "longitud")
    val longitude: Double,

    @Json(name = "momento")
    val lastUpdated: String
)