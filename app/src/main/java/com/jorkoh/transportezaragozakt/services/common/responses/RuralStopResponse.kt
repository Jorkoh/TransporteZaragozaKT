package com.jorkoh.transportezaragozakt.services.common.responses

import com.jorkoh.transportezaragozakt.db.StopDestination

interface RuralStopResponse {
    fun toStopDestinations(busStopId: String): List<StopDestination>
}