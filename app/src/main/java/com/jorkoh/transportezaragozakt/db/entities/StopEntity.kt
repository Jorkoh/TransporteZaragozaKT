package com.jorkoh.transportezaragozakt.db.entities

import androidx.room.*
import com.jorkoh.transportezaragozakt.models.IStop
import com.jorkoh.transportezaragozakt.models.IStopDestination
import com.jorkoh.transportezaragozakt.models.StopType
import java.util.Date

data class CompleteStopEntity(
    @Embedded
    var stopEntity: StopEntity,
    @Relation(parentColumn = "id", entityColumn = "stopId", entity = StopDestinationEntity::class) override val destinations: List<StopDestinationEntity>
) : IStop {
    override val id: String
        get() = stopEntity.id
    override val type: StopType
        get() = stopEntity.type
    override val title: String
        get() = stopEntity.title
}

@Entity(tableName = "stops")
data class StopEntity(
    @PrimaryKey var id: String,
    @ColumnInfo(name = "type") var type: StopType,
    @ColumnInfo(name = "title")  var title: String,
    @ColumnInfo(name = "updatedAt") var updatedAt: Date
)

@Entity(tableName = "destinations")
data class StopDestinationEntity(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "line") override var line: String,
    @ColumnInfo(name = "destination") override var destination: String,
    @ColumnInfo(name = "times") override var times: List<Int>,
    @ColumnInfo(name = "stopId") var stopId : String
) : IStopDestination