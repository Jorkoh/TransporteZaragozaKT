package com.jorkoh.transportezaragozakt.db

import android.util.Log
import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import java.time.Instant
import java.util.*

enum class StopType {
    BUS, TRAM
}

//class StopAndDestinations {
//    @Embedded
//    var stop: Stop? = null
//
//    @Relation(parentColumn = "id", entityColumn = "stopId")
//    var destinations: List<StopDestination> = listOf()
//}

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
    var times: List<Int>,

    @ColumnInfo(name = "updatedAt")
    var updatedAt: Date
)

@Entity(tableName = "favoriteStops")
data class FavoriteStop(
    //TODO: MAKE AN AUTO PRIMARY KEY AND MAKE THIS FOREIGN OR SOMETHING
    @PrimaryKey
    @ColumnInfo(name = "stopId")
    var stopId: String,

    @ColumnInfo(name = "colorHex")
    var colorHex: String
)

//TODO: Move this
fun StopDestination.isFresh(timeoutInSeconds: Int): Boolean  {
    val result = ((Date().time - updatedAt.time)/1000) < timeoutInSeconds
    Log.d("TestingStuff", "Is fresh: $result. Current time: ${Date()}, updated at: $updatedAt")
    return result
}

//TODO: Figure this out
data class TagInfo(val id: String, val type: StopType)