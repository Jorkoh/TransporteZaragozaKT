package com.jorkoh.transportezaragozakt.services.ctaz_api.moshi_adapters

import com.squareup.moshi.*
import java.text.SimpleDateFormat
import java.util.*

class CtazAPITimeAdapter : JsonAdapter<Date>() {
    companion object {
        const val REMAINING_FORMAT = ("HH:mm:ss")
        const val UPDATED_AT_FORMAT = ("yyyy-MM-dd HH:mm:ss")
    }

    private val remainingFormat = SimpleDateFormat(REMAINING_FORMAT, Locale.getDefault())
    private val updatedAtFormat = SimpleDateFormat(UPDATED_AT_FORMAT, Locale.getDefault())

    @FromJson
    override fun fromJson(reader: JsonReader): Date {
        val dateAsString = reader.nextString()
        return if (dateAsString.length > 8) {
            updatedAtFormat.parse(dateAsString)
        } else {
            remainingFormat.parse(dateAsString)
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Date?) {
        if (value != null) {
            writer.value(value.toString())
        }
    }
}