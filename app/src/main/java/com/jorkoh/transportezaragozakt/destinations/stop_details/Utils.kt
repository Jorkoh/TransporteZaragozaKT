package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.jorkoh.transportezaragozakt.db.StopType


fun createStopDetailsDeepLink(stopId: String, stopType: StopType) = Intent(
    Intent.ACTION_VIEW,
    Uri.parse("launchTZ://viewStop/$stopType/$stopId/")
).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK
}

//TODO THIS SHOULDN'T BE HERE
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}