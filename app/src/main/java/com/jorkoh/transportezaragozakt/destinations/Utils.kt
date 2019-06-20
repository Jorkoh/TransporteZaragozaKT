package com.jorkoh.transportezaragozakt.destinations

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.db.StopType

//Includes transparent and black, used for colorpickers
val materialColors = intArrayOf(
    Color.TRANSPARENT,
    -0xbbcca,
    -0x16e19d,
    -0xd36d,
    -0x63d850,
    -0x98c549,
    -0xc0ae4b,
    -0xde690d,
    -0xfc560c,
    -0xff432c,
    -0xff6978,
    -0xb350b0,
    -0x743cb6,
    -0x3223c7,
    -0x14c5,
    -0x3ef9,
    -0x6800,
    -0x86aab8,
    -0x9f8275,
    Color.BLACK
)

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Location.toLatLng() : LatLng = LatLng(latitude, longitude)

fun createStopDetailsDeepLink(stopId: String, stopType: StopType) = Intent(
    Intent.ACTION_VIEW,
    Uri.parse("launchTZ://viewStop/$stopType/$stopId/")
).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK
}

fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}