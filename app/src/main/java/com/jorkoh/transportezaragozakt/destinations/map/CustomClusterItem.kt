package com.jorkoh.transportezaragozakt.destinations.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType

data class CustomClusterItem(
    private val _position: LatLng,
    val type: ClusterItemType,
    val stop: Stop? = null,
    val ruralTracking: RuralTracking? = null
) : ClusterItem {

    enum class ClusterItemType {
        BUS_NORMAL,
        BUS_FAVORITE,
        TRAM_NORMAL,
        TRAM_FAVORITE,
        RURAL_NORMAL,
        RURAL_FAVORITE,
        RURAL_TRACKING
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
        stop = stop
    )

    constructor(ruralTracking: RuralTracking) : this(
        ruralTracking.location,
        ClusterItemType.RURAL_TRACKING,
        ruralTracking = ruralTracking
    )
}