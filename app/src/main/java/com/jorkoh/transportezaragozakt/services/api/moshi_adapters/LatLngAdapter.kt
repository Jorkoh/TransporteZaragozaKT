package com.jorkoh.transportezaragozakt.services.api.moshi_adapters

import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.FromJson

class LatLngAdapter {
    @FromJson
    fun fromJson(coordinates: List<Double>): LatLng {
        return LatLng(coordinates.last(), coordinates.first())
    }
}