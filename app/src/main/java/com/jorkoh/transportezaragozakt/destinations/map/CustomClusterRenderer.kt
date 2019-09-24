package com.jorkoh.transportezaragozakt.destinations.map

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.jorkoh.transportezaragozakt.destinations.map.CustomClusterItem.ClusterItemType.*
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MAX_CLUSTERING_ZOOM
import org.koin.core.KoinComponent
import org.koin.core.inject

class CustomClusterRenderer(
    context: Context,
    private val map: GoogleMap,
    private val clusterManager: ClusterManager<CustomClusterItem>,
    private val selectedItemId: MutableLiveData<String>,
    private val busFilterEnabled: LiveData<Boolean>,
    private val tramFilterEnabled: LiveData<Boolean>,
    private val ruralFilterEnabled: LiveData<Boolean>
) :
    DefaultClusterRenderer<CustomClusterItem>(context, map, clusterManager), GoogleMap.OnCameraIdleListener, KoinComponent {

    private val markerIcons: MarkerIcons by inject()

    private var currentZoom: Float = map.cameraPosition.zoom

    override fun onCameraIdle() {
        currentZoom = map.cameraPosition.zoom
    }

    override fun shouldRenderAsCluster(cluster: Cluster<CustomClusterItem>): Boolean {
        // Avoid clustering if the zoom level is above a threshold or it's just one stop
        // There is a limitation to this, see the issue https://github.com/googlemaps/android-maps-utils/issues/408
        return if (currentZoom >= MAX_CLUSTERING_ZOOM) {
            false
        } else {
            cluster.size > 1
        }
    }

    override fun onBeforeClusterItemRendered(item: CustomClusterItem?, markerOptions: MarkerOptions?) {
        item?.let {
            markerOptions?.visible(
                when (item.type) {
                    BUS_NORMAL, BUS_FAVORITE -> busFilterEnabled.value == true
                    TRAM_NORMAL, TRAM_FAVORITE -> tramFilterEnabled.value == true
                    RURAL_NORMAL, RURAL_FAVORITE, RURAL_TRACKING -> ruralFilterEnabled.value == true
                }
            )
            if (markerOptions?.isVisible == true) {
                markerOptions.icon(item.type.getMarkerIcon(markerIcons))
            }
        }
    }

    override fun onClusterItemRendered(clusterItem: CustomClusterItem?, marker: Marker?) {
        marker?.let {
            //Stop is added as a tag to be able to render the InfoWindow
            marker.tag = clusterItem
            if (selectedItemId.value == clusterItem?.stop?.stopId ?: clusterItem?.ruralTracking?.vehicleId) {
                marker.showInfoWindow()
            }
        }
    }
}