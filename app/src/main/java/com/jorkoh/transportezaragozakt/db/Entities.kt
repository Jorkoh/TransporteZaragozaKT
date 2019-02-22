package com.jorkoh.transportezaragozakt.db

import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.services.api.models.StopType
import java.util.*


class StopAndDestinations {
    @Embedded
    var stop: Stop? = null

    @Relation(parentColumn = "id", entityColumn = "stopId")
    var destinations: List<StopDestination> = listOf()
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
    var location : LatLng
)

@Entity(tableName = "stopDestinations",
    primaryKeys = ["line", "destination", "stopId"],
    foreignKeys = [ForeignKey(entity = Stop::class, parentColumns = ["id"], childColumns = ["stopId"], onDelete = ForeignKey.CASCADE)])
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