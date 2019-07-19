package com.jorkoh.transportezaragozakt.services.web

import android.content.Context
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopDestination
import com.jorkoh.transportezaragozakt.services.common.responses.BusStopResponse
import pl.droidsonroids.jspoon.annotation.Selector
import java.util.*

class BusStopWebResponse: BusStopResponse {

    @Selector("h4")
    lateinit var id: String

    @Selector("[xmlns] tbody:nth-of-type(1) tbody tr:not(:first-child)")
    lateinit var destinations: List<Destination>

    override fun toStopDestinations(context: Context): List<StopDestination> {
        val stopDestinations = mutableListOf<StopDestination>()
        destinations.groupBy { it.line + it.name }.forEach { destinationTimes ->
            stopDestinations += StopDestination(
                destinationTimes.value[0].line.fixLine(),
                destinationTimes.value[0].name,
                id.fixId(),
                listOf(
                    (destinationTimes.value[0].minutes).toMinutes(context),
                    (destinationTimes.value.getOrNull(1)?.minutes ?: "").toMinutes(context)
                ),
                Date()
            )
        }
        return stopDestinations
    }

    private fun String.toMinutes(context: Context): String =
        when (this) {
            "Sin estimacin." -> context.getString(R.string.no_estimate)
            "En la parada." -> context.getString(R.string.at_the_stop)
            else -> {
                when (val minutes = (this.split(" ")[0].toIntOrNull() ?: -1)) {
                    -1 -> context.getString(R.string.no_estimate)
                    1 -> minutes.toString() + " ${context.getString(R.string.minute)}"
                    else -> minutes.toString() + " ${context.getString(R.string.minutes)}"
                }
            }
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