package com.jorkoh.transportezaragozakt.navigation

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout


class AnimableConstraintLayout : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    var xFraction
        get() = if (width == 0) x else x / width.toFloat()
        set(xFraction) {
            x = if (width > 0) xFraction * width else -9999f
        }
}