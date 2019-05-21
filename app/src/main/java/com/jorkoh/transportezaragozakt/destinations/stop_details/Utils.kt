package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.content.Intent
import android.net.Uri
import com.jorkoh.transportezaragozakt.db.StopType


fun createStopDetailsDeepLink(stopId: String, stopType: StopType) = Intent(
    Intent.ACTION_VIEW,
    Uri.parse("launchTZ://viewStop/$stopType/$stopId/")
).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK
}