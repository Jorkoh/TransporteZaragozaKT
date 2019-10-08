package com.jorkoh.transportezaragozakt.destinations

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.Location
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.os.ConfigurationCompat
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.StopType
import java.util.*

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

fun String.officialLineIdToBusWebLineId() = officialToBusWebLineIds[this] ?: ""

val officialToBusWebLineIds = mapOf(
    "21" to "021",
    "22" to "022",
    "23" to "023",
    "24" to "024",
    "25" to "025",
    "28" to "028",
    "29" to "029",
    "30" to "030",
    "31" to "031",
    "32" to "032",
    "33" to "033",
    "34" to "034",
    "35" to "035",
    "36" to "036",
    "38" to "038",
    "39" to "039",
    "40" to "040",
    "41" to "041",
    "42" to "042",
    "43" to "043",
    "44" to "044",
    "50" to "050",
    "51" to "051",
    "52" to "052",
    "53" to "053",
    "54" to "054",
    "55" to "055",
    "56" to "056",
    "57" to "057",
    "58" to "058",
    "59" to "059",
    "60" to "060",
    "C1" to "0C1",
    "C4" to "0C4",
    "Ci1" to "CI1",
    "Ci2" to "CI2",
    "N1" to "N01",
    "N2" to "N02",
    "N3" to "N03",
    "N4" to "N04",
    "N5" to "N05",
    "N6" to "N06",
    "N7" to "N07"
)

// Color is saved as hex in persistence so it has to be masked, there is no Android utility for this available on API >=21.
// Transparent is equivalent to no color selected
fun Int.toHexFromColor() = if (this == Color.TRANSPARENT) "" else String.format("#%06X", 0xFFFFFF and this)

fun String.toColorFromHex() = if (this.isEmpty()) Color.TRANSPARENT else this.toColorInt()

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)

fun createStopDetailsDeepLink(stopId: String, stopType: StopType) = Intent(
    Intent.ACTION_VIEW,
    Uri.parse("launchTZ://viewStop/$stopType/$stopId/")
).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK
}

fun createChangelogDeepLink() = Intent(
    Intent.ACTION_VIEW,
    Uri.parse("launchTZ://viewChangelog/")
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

fun getOnBackgroundColor(context: Context): Int {
    val typedValue = TypedValue()

    val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorOnBackground))
    val color = a.getColor(0, 0)

    a.recycle()

    return color
}

fun CheckBox.setDrawableColor(color: Int) {
    compoundDrawables.filterNotNull().forEach {
        it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}

fun lighter(@ColorInt color: Int, @FloatRange(from = 0.0, to = 1.0) factor: Float = 0.15f): Int {
    val alpha = Color.alpha(color)
    val red = ((Color.red(color) * (1 - factor) / 255 + factor) * 255).toInt()
    val green = ((Color.green(color) * (1 - factor) / 255 + factor) * 255).toInt()
    val blue = ((Color.blue(color) * (1 - factor) / 255 + factor) * 255).toInt()
    return Color.argb(alpha, red, green, blue)
}

fun List<String>.inflateLines(container: GridLayout, stopType: StopType, context: Context) {
    container.removeAllViews()
    forEachIndexed { index, line ->
        LayoutInflater.from(context).inflate(R.layout.map_info_window_line, container)
        val lineView = container.getChildAt(index) as TextView

        val lineColor = when(stopType){
            StopType.BUS -> R.color.bus_color
            StopType.TRAM -> R.color.tram_color
            StopType.RURAL -> R.color.rural_color
        }
        lineView.background.setColorFilter(
            ContextCompat.getColor(context, lineColor),
            PorterDuff.Mode.SRC_IN
        )
        lineView.text = line
        lineView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    container.contentDescription = "${context.getString(R.string.lines)}: ${joinToString(separator = ", ")}"
}

fun Context.isSpanish() = ConfigurationCompat.getLocales(resources.configuration)[0].language == Locale("es").language