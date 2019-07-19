package com.jorkoh.transportezaragozakt.services.common.responses

import android.content.Context
import com.jorkoh.transportezaragozakt.db.StopDestination

interface BusStopResponse {
    fun toStopDestinations(context: Context): List<StopDestination>
}