package com.jorkoh.transportezaragozakt.destinations.search

import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import java.lang.ref.WeakReference

class WeakLocationCallback(locationCallback: LocationCallback) : LocationCallback() {

    private val weakLocationCallback = WeakReference(locationCallback)

    override fun onLocationResult(result: LocationResult) {
        super.onLocationResult(result)
        weakLocationCallback.get()?.onLocationResult(result)
    }

    override fun onLocationAvailability(availability: LocationAvailability?) {
        super.onLocationAvailability(availability)
        weakLocationCallback.get()?.onLocationAvailability(availability)
    }
}