package com.jorkoh.transportezaragozakt.destinations.map

import com.google.android.gms.maps.GoogleMap

class CompositeCameraIdleListener : GoogleMap.OnCameraIdleListener {

    val listeners = mutableListOf<GoogleMap.OnCameraIdleListener>()

    override fun onCameraIdle() {
        listeners.forEach { it.onCameraIdle() }
    }

}