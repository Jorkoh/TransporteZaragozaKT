package com.jorkoh.transportezaragozakt.destinations.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.jorkoh.transportezaragozakt.R

class MarkerIcons(val context: Context){
    val normalBus: BitmapDescriptor
    val favoriteBus: BitmapDescriptor
    val normalTram: BitmapDescriptor
    val favoriteTram: BitmapDescriptor

    init {
        //Initialize the marker icons to be reused on item rendering
        val busDrawable = context.resources.getDrawable(R.drawable.marker_bus, null) as BitmapDrawable
        val busBitmap = Bitmap.createScaledBitmap(
            busDrawable.bitmap,
            MapFragment.ICON_SIZE,
            MapFragment.ICON_SIZE,
            false
        )
        normalBus = BitmapDescriptorFactory.fromBitmap(busBitmap)

        val busFavoriteDrawable = context.resources.getDrawable(R.drawable.marker_bus_favorite, null) as BitmapDrawable
        val busFavoriteBitmap =
            Bitmap.createScaledBitmap(
                busFavoriteDrawable.bitmap,
                MapFragment.ICON_FAV_SIZE,
                MapFragment.ICON_FAV_SIZE,
                false
            )
        favoriteBus = BitmapDescriptorFactory.fromBitmap(busFavoriteBitmap)


        val tramDrawable = context.resources.getDrawable(R.drawable.marker_tram, null) as BitmapDrawable
        val tramBitmap = Bitmap.createScaledBitmap(
            tramDrawable.bitmap,
            MapFragment.ICON_SIZE,
            MapFragment.ICON_SIZE,
            false
        )
        normalTram = BitmapDescriptorFactory.fromBitmap(tramBitmap)

        val tramFavoriteDrawable =
            context.resources.getDrawable(R.drawable.marker_tram_favorite, null) as BitmapDrawable
        val tramFavoriteBitmap =
            Bitmap.createScaledBitmap(
                tramFavoriteDrawable.bitmap,
                MapFragment.ICON_FAV_SIZE,
                MapFragment.ICON_FAV_SIZE,
                false
            )
        favoriteTram = BitmapDescriptorFactory.fromBitmap(tramFavoriteBitmap)
    }
}