package com.jorkoh.transportezaragozakt.db

import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import java.util.*

enum class StopType {
    BUS, TRAM
}

@Entity(tableName = "stops")
data class Stop(
    @ColumnInfo(name = "type")
    var type: StopType,

    @PrimaryKey
    @ColumnInfo(name = "stopId")
    var stopId: String,

    @ColumnInfo(name = "number")
    var number: String,

    @ColumnInfo(name = "stopTitle")
    var stopTitle: String,

    @ColumnInfo(name = "location")
    var location: LatLng,

    @ColumnInfo(name = "lines")
    var lines: List<String>,

    @ColumnInfo(name = "isFavorite")
    var isFavorite: Boolean
) : ClusterItem {
    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return ""
    }

    override fun getPosition(): LatLng = location
}

@Entity(
    tableName = "stopDestinations",
    primaryKeys = ["line", "destination", "stopId"],
    foreignKeys = [ForeignKey(
        entity = Stop::class,
        parentColumns = ["stopId"],
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

    @ColumnInfo(name = "alias")
    var alias: String,

    @ColumnInfo(name = "colorHex")
    var colorHex: String,

    @ColumnInfo(name = "position")
    var position: Int
)

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = Stop::class,
        parentColumns = ["stopId"],
        childColumns = ["stopId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("stopId")]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "reminderId")
    var reminderId: Int,

    @ColumnInfo(name = "stopId")
    var stopId: String,

    @ColumnInfo(name = "type")
    var type: StopType,

    @ColumnInfo(name = "daysOfWeek")
    var daysOfWeek: DaysOfWeek,

    @ColumnInfo(name = "hourOfDay")
    var hourOfDay: Int,

    @ColumnInfo(name = "minute")
    var minute: Int,

    @ColumnInfo(name = "alias")
    var alias: String,

    @ColumnInfo(name = "colorHex")
    var colorHex: String,

    @ColumnInfo(name = "position")
    var position: Int
)

data class FavoritePositions(
    val stopId: String,
    val position: Int
)

data class FavoriteStopExtended(
    val stopId: String,
    val type: StopType,
    val stopTitle: String,
    val alias: String,
    val colorHex: String,
    val lines: List<String>
)

data class StopWithoutLocation(
    val stopId : String,
    val type: StopType,
    val stopTitle: String,
    val lines: List<String>
)

data class ReminderPositions(
    val reminderId: Int,
    val position: Int
)

data class ReminderExtended(
    val reminderId: Int,
    val stopId: String,
    val daysOfWeek: DaysOfWeek,
    val hourOfDay: Int,
    val minute: Int,
    val type: StopType,
    val stopTitle: String,
    val alias: String,
    val colorHex: String,
    val lines: List<String>
)

//https://stackoverflow.com/questions/54938256/typeconverter-not-working-when-updating-listboolean-in-room-database
data class DaysOfWeek(val days : List<Boolean>)