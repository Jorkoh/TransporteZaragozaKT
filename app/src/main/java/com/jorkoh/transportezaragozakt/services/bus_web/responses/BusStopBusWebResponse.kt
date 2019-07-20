package com.jorkoh.transportezaragozakt.services.bus_web.responses

import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.services.bus_web.busWebToOfficialAPIId
import com.jorkoh.transportezaragozakt.services.bus_web.fixLine
import com.jorkoh.transportezaragozakt.services.common.responses.BusStopResponse
import pl.droidsonroids.jspoon.annotation.Selector
import java.util.*

class BusStopBusWebResponse : BusStopResponse {

    @Selector("h4")
    lateinit var id: String

    @Selector("[xmlns] tbody:nth-of-type(1) tbody tr:not(:first-child)")
    var destinations: List<Destination>? = null

    override fun toStopDestinations(): List<StopDestination> {
        val stopDestinations = mutableListOf<StopDestination>()
        destinations?.groupBy { it.line + it.name }?.forEach { destinationTimes ->
            stopDestinations += StopDestination(
                destinationTimes.value[0].line.fixLine(),
                destinationTimes.value[0].name,
                id.busWebToOfficialAPIId(),
                listOf(
                    (destinationTimes.value[0].minutes),
                    (destinationTimes.value.getOrNull(1)?.minutes ?: "")
                ),
                Date()
            )
        }
        return stopDestinations
    }
}

class Destination {
    @Selector(" .digital:nth-of-type(2)")
    lateinit var name: String

    @Selector(" .digital:nth-of-type(1)")
    lateinit var line: String

    @Selector(" .digital:nth-of-type(3)")
    lateinit var minutes: String
}