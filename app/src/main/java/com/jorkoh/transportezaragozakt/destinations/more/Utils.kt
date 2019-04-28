package com.jorkoh.transportezaragozakt.destinations.more

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange

fun lighter(@ColorInt color: Int, @FloatRange(from = 0.0, to = 1.0) factor: Float = 0.15f): Int {
    val alpha = Color.alpha(color)
    val red = ((Color.red(color) * (1 - factor) / 255 + factor) * 255).toInt()
    val green = ((Color.green(color) * (1 - factor) / 255 + factor) * 255).toInt()
    val blue = ((Color.blue(color) * (1 - factor) / 255 + factor) * 255).toInt()
    return Color.argb(alpha, red, green, blue)
}