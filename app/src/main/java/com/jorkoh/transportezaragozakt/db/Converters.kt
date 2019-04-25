package com.jorkoh.transportezaragozakt.db

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.koin.standalone.KoinComponent
import java.util.*

class Converters {

    private val intListAdapter: JsonAdapter<List<Int>> =
            Moshi.Builder().build().adapter(Types.newParameterizedType(List::class.java, Int::class.javaObjectType))
    private val booleanListAdapter: JsonAdapter<List<Boolean>> =
        Moshi.Builder().build().adapter(Types.newParameterizedType(List::class.java, Boolean::class.javaObjectType))
    private val stringListAdapter: JsonAdapter<List<String>> =
        Moshi.Builder().build().adapter(Types.newParameterizedType(List::class.java, String::class.javaObjectType))
    private val doubleListAdapter: JsonAdapter<List<Double>> =
        Moshi.Builder().build().adapter(Types.newParameterizedType(List::class.java, Double::class.javaObjectType))

    @TypeConverter
    fun timestampToDate(value: Long?): Date? {
        return value?.let { Date(it * 1000) }
    }

    @TypeConverter
    fun dateToTimestamp(value: Date?): Long? {
        return value?.let { it.time / 1000 }
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
    fun booleanListToJson(value: List<Boolean>): String {
        return booleanListAdapter.toJson(value)
    }

    @TypeConverter
    fun jsonToBooleanList(value: String): List<Boolean>? {
        return booleanListAdapter.fromJson(value)
    }

    @TypeConverter
    fun stringListToJson(value: List<String>): String {
        return stringListAdapter.toJson(value)
    }

    @TypeConverter
    fun jsonToStringList(value: String): List<String>? {
        return stringListAdapter.fromJson(value)
    }

    @TypeConverter
    fun latLongToJson(value: LatLng): String {
        return doubleListAdapter.toJson(listOf(value.latitude, value.longitude))
    }

    @TypeConverter
    fun jsonToLatLng(value: String): LatLng? {
        val doubleList = doubleListAdapter.fromJson(value)
        return if (doubleList == null) null else LatLng(doubleList[0], doubleList[1])
    }

    @TypeConverter
    fun stopTypeToName(value: StopType): String = value.name

    @TypeConverter
    fun nameToStopType(value: String): StopType = StopType.valueOf(value)
}