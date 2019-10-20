package com.jorkoh.transportezaragozakt.destinations.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType

data class CustomClusterItem(
    private val _position: LatLng,
    val type: ClusterItemType,
    val itemId: String,
    val stop: Stop? = null,
    val ruralTracking: RuralTracking? = null
) : ClusterItem {

    override fun getTypeOrdinal() = type.ordinal

    enum class ClusterItemType {
        BUS_NORMAL {
            override fun isBus() = true

            override fun isTram() = false

            override fun isRural() = false

            override fun isStop() = true
        },
        BUS_FAVORITE {
            override fun isBus() = true

            override fun isTram() = false

            override fun isRural() = false

            override fun isStop() = true
        },
        TRAM_NORMAL {
            override fun isBus() = false

            override fun isTram() = true

            override fun isRural() = false

            override fun isStop() = true
        },
        TRAM_FAVORITE {
            override fun isBus() = false

            override fun isTram() = true

            override fun isRural() = false

            override fun isStop() = true
        },
        RURAL_NORMAL {
            override fun isBus() = false

            override fun isTram() = false

            override fun isRural() = true

            override fun isStop() = true
        },
        RURAL_FAVORITE {
            override fun isBus() = false

            override fun isTram() = false

            override fun isRural() = true

            override fun isStop() = true
        },
        RURAL_TRACKING {
            override fun isBus() = false

            override fun isTram() = false

            override fun isRural() = true

            override fun isStop() = false
        };

        abstract fun isStop(): Boolean
        abstract fun isBus(): Boolean
        abstract fun isTram(): Boolean
        abstract fun isRural(): Boolean
    }

    override fun getSnippet() = ""

    override fun getTitle() = ""

    override fun getPosition() = _position

    constructor(stop: Stop) : this(
        stop.location,
        when (stop.type) {
            StopType.BUS -> if (stop.isFavorite) ClusterItemType.BUS_FAVORITE else ClusterItemType.BUS_NORMAL
            StopType.TRAM -> if (stop.isFavorite) ClusterItemType.TRAM_FAVORITE else ClusterItemType.TRAM_NORMAL
            StopType.RURAL -> if (stop.isFavorite) ClusterItemType.RURAL_FAVORITE else ClusterItemType.RURAL_NORMAL
        },
        stop.stopId,
        stop = stop
    )

    constructor(ruralTracking: RuralTracking) : this(
        ruralTracking.location,
        ClusterItemType.RURAL_TRACKING,
        ruralTracking.vehicleId,
        ruralTracking = ruralTracking
    )
}