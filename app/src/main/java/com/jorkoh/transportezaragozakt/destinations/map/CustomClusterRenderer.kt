package com.jorkoh.transportezaragozakt.destinations.map

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MAX_CLUSTERING_ZOOM
import org.koin.core.KoinComponent
import org.koin.core.inject

class CustomClusterRenderer(val context: Context, val map: GoogleMap, clusterManager: ClusterManager<Stop>) :
    DefaultClusterRenderer<Stop>(context, map, clusterManager), GoogleMap.OnCameraIdleListener, KoinComponent {

    private val markerIcons: MarkerIcons by inject()

    private var currentZoom: Float = map.cameraPosition.zoom

    override fun onCameraIdle() {
        currentZoom = map.cameraPosition.zoom
    }

    override fun shouldRenderAsCluster(cluster: Cluster<Stop>): Boolean {
        // Avoid clustering if the zoom level is above a threshold or it's just one stop
        // There is a limitation to this, see the issue https://github.com/googlemaps/android-maps-utils/issues/408
        return if (currentZoom >= MAX_CLUSTERING_ZOOM) {
            false
        } else {
            cluster.size > 1
        }
    }

    override fun onBeforeClusterItemRendered(stop: Stop?, markerOptions: MarkerOptions?) {
        stop?.let {
            markerOptions?.icon(
                when (stop.type) {
                    StopType.BUS -> if (stop.isFavorite) markerIcons.favoriteBus else markerIcons.normalBus
                    StopType.TRAM -> if (stop.isFavorite) markerIcons.favoriteTram else markerIcons.normalTram
                }
            )
        }
    }

    override fun onClusterItemRendered(clusterItem: Stop?, marker: Marker?) {
        marker?.tag = clusterItem
    }
}