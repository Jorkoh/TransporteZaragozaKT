package com.jorkoh.transportezaragozakt.services.common.responses

import com.jorkoh.transportezaragozakt.db.StopDestination

interface BusStopResponse {
    fun toStopDestinations(): List<StopDestination>
}