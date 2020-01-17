package com.jorkoh.transportezaragozakt.destinations.utils

import android.animation.TimeInterpolator
import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Transition
import androidx.transition.TransitionSet


const val ANIMATE_OUT_OF_DETAILS_SCREEN_DURATION = 325L
const val ANIMATE_INTO_DETAILS_SCREEN_DURATION = 285L

/**
 * Standard easing.
 *
 * Elements that begin and end at rest use standard easing. They speed up quickly and slow down
 * gradually, in order to emphasize the end of the transition.
 */
val FAST_OUT_SLOW_IN: TimeInterpolator by lazy(LazyThreadSafetyMode.NONE) {
    PathInterpolatorCompat.create(0.4f, 0f, 0.2f, 1f)
}

/**
 * Decelerate easing.
 *
 * Incoming elements are animated using deceleration easing, which starts a transition at peak
 * velocity (the fastest point of an element’s movement) and ends at rest.
 */
val LINEAR_OUT_SLOW_IN: TimeInterpolator by lazy(LazyThreadSafetyMode.NONE) {
    PathInterpolatorCompat.create(0f, 0f, 0.2f, 1f)
}

/**
 * Accelerate easing.
 *
 * Elements exiting a screen use acceleration easing, where they start at rest and end at peak
 * velocity.
 */
val FAST_OUT_LINEAR_IN: TimeInterpolator by lazy(LazyThreadSafetyMode.NONE) {
    PathInterpolatorCompat.create(0.4f, 0f, 1f, 1f)
}

inline fun transitionTogether(crossinline body: TransitionSet.() -> Unit): TransitionSet {
    return TransitionSet().apply {
        ordering = TransitionSet.ORDERING_TOGETHER
        body()
    }
}

inline fun TransitionSet.forEach(action: (transition: Transition) -> Unit) {
    for (i in 0 until transitionCount) {
        action(getTransitionAt(i) ?: throw IndexOutOfBoundsException())
    }
}

inline fun TransitionSet.forEachIndexed(action: (index: Int, transition: Transition) -> Unit) {
    for (i in 0 until transitionCount) {
        action(i, getTransitionAt(i) ?: throw IndexOutOfBoundsException())
    }
}

operator fun TransitionSet.iterator() = object : MutableIterator<Transition> {

    private var index = 0

    override fun hasNext() = index < transitionCount

    override fun next() =
        getTransitionAt(index++) ?: throw IndexOutOfBoundsException()

    override fun remove() {
        removeTransition(getTransitionAt(--index) ?: throw IndexOutOfBoundsException())
    }
}

operator fun TransitionSet.plusAssign(transition: Transition?) {
    if (transition != null) {
        addTransition(transition)
    }
}

operator fun TransitionSet.get(i: Int): Transition {
    return getTransitionAt(i) ?: throw IndexOutOfBoundsException()
}

fun View.doAfterLayout(what: () -> Unit) {
    if (isLaidOut) {
        what.invoke()
    } else {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                what.invoke()
            }
        })
    }
}

fun View.slideUpToShow() {
    animate().apply {
        translationY(0f)
        duration = ANIMATE_OUT_OF_DETAILS_SCREEN_DURATION / 2
        interpolator = FAST_OUT_LINEAR_IN
        withStartAction {
            visibility = View.VISIBLE
        }
    }
}

fun View.slideDownToHide() {
    animate().apply {
        translationY(height.toFloat())
        interpolator = LINEAR_OUT_SLOW_IN
        duration = ANIMATE_INTO_DETAILS_SCREEN_DURATION / 2
        withEndAction {
            visibility = View.GONE
            translationY = height.toFloat()
        }
    }
}

// Event class used to notify the removal of fake views used on shared element transitions
data class RemoveFakeTransitionViewEvent(val empty: Unit = Unit)

class NotifyingLinearLayoutManager(context: Context, val callback: () -> Unit) : LinearLayoutManager(context, VERTICAL, false) {
    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        callback()
    }
}

fun View.afterMeasured(function: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                function()
            }
        }
    })
}