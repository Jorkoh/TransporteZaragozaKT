package com.jorkoh.transportezaragozakt.db

import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import java.util.*

enum class StopType {
    BUS, TRAM, RURAL
}

enum class LineType {
    BUS, TRAM, RURAL
}

fun StopType.toLineType() = when (this) {
    StopType.BUS -> LineType.BUS
    StopType.TRAM -> LineType.TRAM
    StopType.RURAL -> LineType.RURAL
}

fun LineType.toStopType() = when (this) {
    LineType.BUS -> StopType.BUS
    LineType.TRAM -> StopType.TRAM
    LineType.RURAL -> StopType.RURAL
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
)

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

    @ColumnInfo(name = "areTrackedTimes")
    var areTrackedTimes: List<String>,

    @ColumnInfo(name = "updatedAt")
    var updatedAt: Date
)

@Entity(tableName = "lines")
data class Line(
    @PrimaryKey
    @ColumnInfo(name = "lineId")
    var lineId: String,

    @ColumnInfo(name = "parentLineId")
    var parentLineId: String?,

    @ColumnInfo(name = "type")
    var type: LineType,

    @ColumnInfo(name = "nameES")
    var nameES: String,

    @ColumnInfo(name = "nameEN")
    var nameEN: String,

    // If the destination is unique this list will be of length one and stopIdsSecondDestination will be empty
    @ColumnInfo(name = "destinations")
    var destinations: List<String>,

    @ColumnInfo(name = "stopIdsFirstDestination")
    var stopIdsFirstDestination: List<String>,

    @ColumnInfo(name = "stopIdsSecondDestination")
    var stopIdsSecondDestination: List<String>
)

@Entity(
    tableName = "lineLocations",
    primaryKeys = ["lineId", "position"]
)
data class LineLocation(
    @ColumnInfo(name = "lineId")
    var lineId: String,

    @ColumnInfo(name = "type")
    var type: LineType,

    @ColumnInfo(name = "position")
    var position: Int,

    @ColumnInfo(name = "location")
    var location: LatLng
)

@Entity(tableName = "ruralTrackings")
data class RuralTracking(
    @PrimaryKey
    @ColumnInfo(name = "vehicleId")
    var vehicleId: String,

    @ColumnInfo(name = "lineId")
    var lineId: String,

    @ColumnInfo(name = "lineName")
    var lineName: String,

    @ColumnInfo(name = "location")
    var location: LatLng,

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

data class StopWithDistance(
    val stop: Stop,
    val distance: Float
)

data class FavoriteStopExtended(
    val stopId: String,
    val type: StopType,
    val number: String,
    val stopTitle: String,
    val alias: String,
    val colorHex: String,
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
data class DaysOfWeek(val days: List<Boolean>) {
    init {
        require(days.count() == 7) {
            "Argument has to be a list of exactly 7 days"
        }
    }
}