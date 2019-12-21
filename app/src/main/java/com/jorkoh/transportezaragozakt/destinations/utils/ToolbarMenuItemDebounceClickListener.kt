package com.jorkoh.transportezaragozakt.destinations.utils

import android.os.SystemClock
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar


class ToolbarMenuItemDebounceClickListener(
    private val debounceInterval: Long = DEBOUNCE_INTERVAL_DEFAULT,
    private val methodToCall: (item: MenuItem) -> Boolean
) : Toolbar.OnMenuItemClickListener {

    companion object {
        private const val DEBOUNCE_INTERVAL_DEFAULT: Long = 200
        private var lastClickTime: Long = 0
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (SystemClock.elapsedRealtime() - lastClickTime < debounceInterval) {
            return false
        }
        lastClickTime = SystemClock.elapsedRealtime()
        return methodToCall(item)
    }

}