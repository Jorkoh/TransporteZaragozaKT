package com.jorkoh.transportezaragozakt.destinations.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import com.jorkoh.transportezaragozakt.R
import kotlin.math.min

/**
 * Custom behavior that moves stop_details_fab up when a Snackbar appears and moves it back down as it disappears,
 * created as a workaround to coordinator layout only notifying direct children. Since the Snackbars are part of the
 * main_container Activity layout and the FAB is part of the StopDetails Fragment layout a direct children relation
 * was impossible.
 *
 * Initially the FAB was part of the main_container layout to work around this limitation but this caused problems
 * with nested fragments not being drawn until the FAB finished its transition animation.
 */
class NestedFabCoordinatorBehavior(context: Context?, attrs: AttributeSet?) : CoordinatorLayout.Behavior<FrameLayout>() {

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: FrameLayout, dependency: View): Boolean {
        val translationY: Float = min(0f, dependency.translationY - dependency.height)
        // Note that the RelativeLayout gets translated.
        child.findViewById<View>(R.id.stop_details_fab)?.translationY = translationY
        return true
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: FrameLayout, dependency: View) {
        child.findViewById<View>(R.id.stop_details_fab)?.translationY = 0.0f
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FrameLayout, dependency: View): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }
}