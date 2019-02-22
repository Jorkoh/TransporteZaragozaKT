package com.jorkoh.transportezaragozakt.services.api.models

import com.google.android.gms.maps.model.LatLng

enum class StopType {
    BUS, TRAM
}

interface IStop {
    val id: String
    val type: StopType
    val title: String
    val destinations: List<IStopDestination>
}

interface IStopDestination {
    val line: String
    val destination: String
    val times: List<Int>
}

//TODO ROOM: Figure how to work locations into this
interface IStopLocation {
    val stopId: String
    val coordinates: LatLng
}