package com.jorkoh.transportezaragozakt

import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.db.Converters
import com.jorkoh.transportezaragozakt.db.DaysOfWeek
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*

class ConvertersTest {

    @Test(expected = IllegalArgumentException::class)
    fun daysOfWeek_emptyList() {
        DaysOfWeek(emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun daysOfWeek_wrongLength() {
        DaysOfWeek(listOf(true, false, true))
    }

    @Test
    fun daysOfWeekToJsonConverter_bothWays() {
        val originalDaysOfWeek = DaysOfWeek(listOf(true, true, true, true, true, false, false))
        val jsonDaysOfWeek = Converters.daysOfWeekToJson(originalDaysOfWeek)
        val convertedDaysOfWeek = Converters.jsonToDaysOfWeek(jsonDaysOfWeek)

        assertThat(convertedDaysOfWeek, `is`(originalDaysOfWeek))
    }

    @Test
    fun dateToTimestampConverter_bothWays() {
        val originalDate = Date()
        val timestampDate = Converters.dateToTimestamp(originalDate)
        val convertedDate = Converters.timestampToDate(timestampDate)

        assertThat(convertedDate, `is`(originalDate))
    }

    @Test
    fun latLngToJsonConverter_bothWays(){
        val originalLatLng = LatLng(41.656362, -0.878920)
        val jsonLatLng = Converters.latLongToJson(originalLatLng)
        val convertedLatLng = Converters.jsonToLatLng(jsonLatLng)

        assertThat(convertedLatLng, `is`(originalLatLng))
    }
}