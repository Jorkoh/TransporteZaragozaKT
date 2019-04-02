package com.jorkoh.transportezaragozakt.db

import android.util.Log
import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import java.util.*

enum class StopType {
    BUS, TRAM
}

@Entity(tableName = "stops")
data class Stop(
    @ColumnInfo(name = "type")
    var type: StopType,

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: String,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "location")
    var location: LatLng,

    @ColumnInfo(name = "isFavorite")
    var isFavorite: Boolean
)

@Entity(
    tableName = "stopDestinations",
    primaryKeys = ["line", "destination", "stopId"],
    foreignKeys = [ForeignKey(
        entity = Stop::class,
        parentColumns = ["id"],
        childColumns = ["stopId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("stopId")]
)
data class StopDestination(
    @ColumnInfo(name = "line")
    var line: String,

    @ColumnInfo(name = "destination")
    var destination: String,

    @ColumnInfo(name = "stopId")
    var stopId: String,

    @ColumnInfo(name = "times")
    var times: List<String>,

    @ColumnInfo(name = "updatedAt")
    var updatedAt: Date
)

@Entity(tableName = "favoriteStops")
data class FavoriteStop(
    @PrimaryKey
    @ColumnInfo(name = "stopId")
    var stopId: String,

    @ColumnInfo(name = "colorHex")
    var colorHex: String
)

data class TagInfo(val id: String, val type: StopType)