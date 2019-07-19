package com.jorkoh.transportezaragozakt.services.web.responses

import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.services.common.responses.BusStopResponse
import pl.droidsonroids.jspoon.annotation.Selector
import java.util.*

class BusStopWebResponse: BusStopResponse {

    @Selector("h4")
    lateinit var id: String

    @Selector("[xmlns] tbody:nth-of-type(1) tbody tr:not(:first-child)")
    lateinit var destinations: List<Destination>

    override fun toStopDestinations(): List<StopDestination> {
        val stopDestinations = mutableListOf<StopDestination>()
        destinations.groupBy { it.line + it.name }.forEach { destinationTimes ->
            stopDestinations += StopDestination(
                destinationTimes.value[0].line.fixLine(),
                destinationTimes.value[0].name,
                id.fixId(),
                listOf(
                    (destinationTimes.value[0].minutes),
                    (destinationTimes.value.getOrNull(1)?.minutes ?: "")
                ),
                Date()
            )
        }
        return stopDestinations
    }



    private fun String.fixLine() =
        when (this) {
            "CI1" -> "Ci1"
            "CI2" -> "Ci2"
            else -> this
        }

    private fun String.fixId() =
        "tuzsa-${this.split(" ")[1]}"
}

class Destination{
    @Selector(" .digital:nth-of-type(2)")
    lateinit var name: String

    @Selector(" .digital:nth-of-type(1)")
    lateinit var line: String

    @Selector(" .digital:nth-of-type(3)")
    lateinit var minutes: String
}