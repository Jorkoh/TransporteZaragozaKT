package com.jorkoh.transportezaragozakt.destinations.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.jorkoh.transportezaragozakt.R

class MarkerIcons(val context: Context) {
    val busNormal: BitmapDescriptor
    val busFavorite: BitmapDescriptor
    val tramNormal: BitmapDescriptor
    val tramFavorite: BitmapDescriptor
    val ruralNormal: BitmapDescriptor
    val ruralFavorite: BitmapDescriptor
    val ruralTracking: BitmapDescriptor

    init {
        //Initialize the marker icons to be reused on item rendering
        val busDrawable = context.resources.getDrawable(R.drawable.marker_bus, null) as BitmapDrawable
        val busBitmap = Bitmap.createScaledBitmap(
            busDrawable.bitmap,
            MapFragment.ICON_SIZE,
            MapFragment.ICON_SIZE,
            false
        )
        busNormal = BitmapDescriptorFactory.fromBitmap(busBitmap)

        val busFavoriteDrawable = context.resources.getDrawable(R.drawable.marker_bus_favorite, null) as BitmapDrawable
        val busFavoriteBitmap =
            Bitmap.createScaledBitmap(
                busFavoriteDrawable.bitmap,
                MapFragment.ICON_FAV_SIZE,
                MapFragment.ICON_FAV_SIZE,
                false
            )
        busFavorite = BitmapDescriptorFactory.fromBitmap(busFavoriteBitmap)


        val tramDrawable = context.resources.getDrawable(R.drawable.marker_tram, null) as BitmapDrawable
        val tramBitmap = Bitmap.createScaledBitmap(
            tramDrawable.bitmap,
            MapFragment.ICON_SIZE,
            MapFragment.ICON_SIZE,
            false
        )
        tramNormal = BitmapDescriptorFactory.fromBitmap(tramBitmap)

        val tramFavoriteDrawable =
            context.resources.getDrawable(R.drawable.marker_tram_favorite, null) as BitmapDrawable
        val tramFavoriteBitmap =
            Bitmap.createScaledBitmap(
                tramFavoriteDrawable.bitmap,
                MapFragment.ICON_FAV_SIZE,
                MapFragment.ICON_FAV_SIZE,
                false
            )
        tramFavorite = BitmapDescriptorFactory.fromBitmap(tramFavoriteBitmap)

        val ruralDrawable = context.resources.getDrawable(R.drawable.marker_rural, null) as BitmapDrawable
        val ruralBitmap = Bitmap.createScaledBitmap(
            ruralDrawable.bitmap,
            MapFragment.ICON_SIZE,
            MapFragment.ICON_SIZE,
            false
        )
        ruralNormal = BitmapDescriptorFactory.fromBitmap(ruralBitmap)

        val ruralFavoriteDrawable =
            context.resources.getDrawable(R.drawable.marker_rural_favorite, null) as BitmapDrawable
        val ruralFavoriteBitmap =
            Bitmap.createScaledBitmap(
                ruralFavoriteDrawable.bitmap,
                MapFragment.ICON_FAV_SIZE,
                MapFragment.ICON_FAV_SIZE,
                false
            )
        ruralFavorite = BitmapDescriptorFactory.fromBitmap(ruralFavoriteBitmap)

        val ruralTrackingDrawable =
            context.resources.getDrawable(R.drawable.marker_rural_tracking, null) as BitmapDrawable
        val ruralTrackingBitmap =
            Bitmap.createScaledBitmap(
                ruralTrackingDrawable.bitmap,
                MapFragment.ICON_TRACKING_SIZE,
                MapFragment.ICON_TRACKING_SIZE,
                false
            )
        ruralTracking = BitmapDescriptorFactory.fromBitmap(ruralTrackingBitmap)
    }
}

fun CustomClusterItem.ClusterItemType.getMarkerIcon(markerIcons: MarkerIcons) =
    when (this) {
        CustomClusterItem.ClusterItemType.BUS_NORMAL -> markerIcons.busNormal
        CustomClusterItem.ClusterItemType.BUS_FAVORITE -> markerIcons.busFavorite
        CustomClusterItem.ClusterItemType.TRAM_NORMAL -> markerIcons.tramNormal
        CustomClusterItem.ClusterItemType.TRAM_FAVORITE -> markerIcons.tramFavorite
        CustomClusterItem.ClusterItemType.RURAL_NORMAL -> markerIcons.ruralNormal
        CustomClusterItem.ClusterItemType.RURAL_FAVORITE -> markerIcons.ruralFavorite
        CustomClusterItem.ClusterItemType.RURAL_TRACKING -> markerIcons.ruralTracking
    }