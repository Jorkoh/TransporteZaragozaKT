package com.jorkoh.transportezaragozakt.db

import androidx.room.TypeConverter
import com.jorkoh.transportezaragozakt.models.StopType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*

class Converters : KoinComponent{

    private val intListAdapter: JsonAdapter<List<Int>> by inject()

    @TypeConverter
    fun timestampToDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(value: Date?): Long? {
        return value?.time
    }

    @TypeConverter
    fun intListToJson(value: List<Int>): String {
        return intListAdapter.toJson(value)
    }

    @TypeConverter
    fun jsonToIntList(value: String): List<Int>? {
        return intListAdapter.fromJson(value)
    }

    @TypeConverter
    fun stopTypeToName(value: StopType): String = value.name

    @TypeConverter
    fun nameToStopType(value: String): StopType = StopType.valueOf(value)
}