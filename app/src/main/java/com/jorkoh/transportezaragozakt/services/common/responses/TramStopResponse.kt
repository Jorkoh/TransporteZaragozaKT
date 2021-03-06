package com.jorkoh.transportezaragozakt.services.common.responses

import com.jorkoh.transportezaragozakt.db.StopDestination

interface TramStopResponse {
    fun toStopDestinations(tramStopId: String): List<StopDestination>
}