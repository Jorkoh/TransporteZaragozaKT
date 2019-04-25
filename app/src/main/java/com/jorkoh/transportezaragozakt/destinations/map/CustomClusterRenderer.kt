package com.jorkoh.transportezaragozakt.destinations.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ShapeDrawable
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.DEFAULT_ZOOM
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.MAX_ZOOM

class CustomClusterRenderer(val context: Context, val map: GoogleMap, clusterManager: ClusterManager<Stop>) :
    DefaultClusterRenderer<Stop>(context, map, clusterManager), GoogleMap.OnCameraIdleListener {

    private lateinit var busMarkerIcon: BitmapDescriptor
    private lateinit var busFavoriteMarkerIcon: BitmapDescriptor
    private lateinit var tramMarkerIcon: BitmapDescriptor
    private lateinit var tramFavoriteMarkerIcon: BitmapDescriptor

    private var currentZoom: Float = DEFAULT_ZOOM


    init {
        createBaseMarkers()
    }

    override fun onCameraIdle() {
        currentZoom = map.cameraPosition.zoom
    }


    override fun shouldRenderAsCluster(cluster: Cluster<Stop>): Boolean {
        return if (currentZoom >= DEFAULT_ZOOM) {
            false
        } else {
            cluster.size > 1
        }
    }

    override fun onBeforeClusterItemRendered(stop: Stop?, markerOptions: MarkerOptions?) {
        stop?.let {
            markerOptions?.icon(
                when (stop.type) {
                    StopType.BUS -> if (stop.isFavorite) busFavoriteMarkerIcon else busMarkerIcon
                    StopType.TRAM -> if (stop.isFavorite) tramFavoriteMarkerIcon else tramMarkerIcon
                }
            )
        }
    }

    private fun createBaseMarkers() {
        val busDrawable = context.resources.getDrawable(R.drawable.marker_bus, null) as BitmapDrawable
        val busBitmap = Bitmap.createScaledBitmap(
            busDrawable.bitmap,
            MapFragment.ICON_SIZE,
            MapFragment.ICON_SIZE,
            false
        )
        busMarkerIcon = BitmapDescriptorFactory.fromBitmap(busBitmap)

        val busFavoriteDrawable = context.resources.getDrawable(R.drawable.marker_bus_favorite, null) as BitmapDrawable
        val busFavoriteBitmap =
            Bitmap.createScaledBitmap(
                busFavoriteDrawable.bitmap,
                MapFragment.ICON_FAV_SIZE,
                MapFragment.ICON_FAV_SIZE,
                false
            )
        busFavoriteMarkerIcon = BitmapDescriptorFactory.fromBitmap(busFavoriteBitmap)


        val tramDrawable = context.resources.getDrawable(R.drawable.marker_tram, null) as BitmapDrawable
        val tramBitmap = Bitmap.createScaledBitmap(
            tramDrawable.bitmap,
            MapFragment.ICON_SIZE,
            MapFragment.ICON_SIZE,
            false
        )
        tramMarkerIcon = BitmapDescriptorFactory.fromBitmap(tramBitmap)

        val tramFavoriteDrawable =
            context.resources.getDrawable(R.drawable.marker_tram_favorite, null) as BitmapDrawable
        val tramFavoriteBitmap =
            Bitmap.createScaledBitmap(
                tramFavoriteDrawable.bitmap,
                MapFragment.ICON_FAV_SIZE,
                MapFragment.ICON_FAV_SIZE,
                false
            )
        tramFavoriteMarkerIcon = BitmapDescriptorFactory.fromBitmap(tramFavoriteBitmap)
    }
}