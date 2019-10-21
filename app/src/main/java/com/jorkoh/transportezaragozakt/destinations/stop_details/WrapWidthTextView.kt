package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.content.Context
import android.text.Layout
import android.util.AttributeSet
import android.widget.TextView
import kotlin.math.ceil


class WrapWidthTextView(context: Context, attrs: AttributeSet) : TextView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        if (widthMode != MeasureSpec.EXACTLY) {
            if (layout != null) {
                val maxWidth = ceil(getMaxLineWidth(layout)).toInt() + compoundPaddingLeft + compoundPaddingRight
                if (maxWidth < measuredWidth) {
                    super.onMeasure(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST), heightMeasureSpec)
                }
            }
        }
    }

    private fun getMaxLineWidth(layout: Layout): Float {
        var maxWidth = 0.0f
        for (i in 0 until layout.lineCount) {
            if (layout.getLineWidth(i) > maxWidth) {
                maxWidth = layout.getLineWidth(i)
            }
        }
        return maxWidth
    }
}