package com.jorkoh.transportezaragozakt.db

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.util.*

object Converters {

    private val intListAdapter: JsonAdapter<List<Int>> =
        Moshi.Builder().build().adapter(Types.newParameterizedType(List::class.java, Int::class.javaObjectType))
    private val stringListAdapter: JsonAdapter<List<String>> =
        Moshi.Builder().build().adapter(Types.newParameterizedType(List::class.java, String::class.javaObjectType))
    private val doubleListAdapter: JsonAdapter<List<Double>> =
        Moshi.Builder().build().adapter(Types.newParameterizedType(List::class.java, Double::class.javaObjectType))

    @TypeConverter
    @JvmStatic
    fun timestampToDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(value: Date?): Long? {
        return value?.time
    }

    @TypeConverter
    @JvmStatic
    fun intListToJson(value: List<Int>): String {
        return intListAdapter.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun jsonToIntList(value: String): List<Int>? {
        return intListAdapter.fromJson(value)
    }

    //https://stackoverflow.com/questions/54938256/typeconverter-not-working-when-updating-listboolean-in-room-database
    @TypeConverter
    @JvmStatic
    fun daysOfWeekToJson(value: DaysOfWeek): String {
        val sb = StringBuilder()
        value.days.forEach {
            sb.append(if (it) 'T' else 'F')
        }
        return sb.toString()
    }

    //https://stackoverflow.com/questions/54938256/typeconverter-not-working-when-updating-listboolean-in-room-database
    @TypeConverter
    @JvmStatic
    fun jsonToDaysOfWeek(value: String): DaysOfWeek? {
        val result = mutableListOf<Boolean>()
        value.forEach {
            result.add(it == 'T')
        }
        return DaysOfWeek(result)
    }

    @TypeConverter
    @JvmStatic
    fun stringListToJson(value: List<String>): String {
        return stringListAdapter.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun jsonToStringList(value: String): List<String>? {
        return stringListAdapter.fromJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun latLongToJson(value: LatLng): String = doubleListAdapter.toJson(listOf(value.latitude, value.longitude))

    @TypeConverter
    @JvmStatic
    fun jsonToLatLng(value: String): LatLng? = doubleListAdapter.fromJson(value)?.let { LatLng(it[0], it[1]) }

    @TypeConverter
    @JvmStatic
    fun stopTypeToName(value: StopType): String = value.name

    @TypeConverter
    @JvmStatic
    fun nameToStopType(value: String): StopType = StopType.valueOf(value)

    @TypeConverter
    @JvmStatic
    fun lineTypeToName(value: LineType): String = value.name

    @TypeConverter
    @JvmStatic
    fun nameToLineType(value: String): LineType = LineType.valueOf(value)
}