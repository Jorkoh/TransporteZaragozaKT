/*
 * Copyright (C) 2018 Jared Rummler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jaredrummler.cyanea.inflator

import android.R.attr
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.view.menu.ListMenuItemView
import androidx.appcompat.widget.AlertDialogLayout
import androidx.appcompat.widget.AppCompatDrawableManager
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.R
import com.jaredrummler.cyanea.delegate.CyaneaDelegate
import com.jaredrummler.cyanea.tinting.EdgeEffectTint
import com.jaredrummler.cyanea.tinting.WidgetTint
import com.jaredrummler.cyanea.utils.ColorUtils
import com.jaredrummler.cyanea.utils.Reflection

/**
 * Class to process views in connection with the [CyaneaLayoutInflater]. To add a [CyaneaViewProcessor] you must
 * let your application or activity implement [CyaneaViewProcessor.Provider]. When a view is created it will
 * call [CyaneaViewProcessor.process] if [CyaneaViewProcessor.shouldProcessView] returns true.
 */
abstract class CyaneaViewProcessor<T : View> {

    /**
     * Process a newly created view.
     *
     * @param view
     * The newly created view.
     * @param attrs
     * The view's [attributes][AttributeSet]
     * @param cyanea
     * The [cyanea][Cyanea] instance used for styling views.
     */
    abstract fun process(view: T, attrs: AttributeSet?, cyanea: Cyanea)

    /**
     * Check if a view should be processed. By default, this checks if the view is an instance of [.getType].
     *
     * @param view
     * The view to check
     * @return True if this view should be processed.
     */
    open fun shouldProcessView(view: View) = getType().isInstance(view)

    /**
     * The class for the given view
     *
     * @return The class for T
     */
    protected abstract fun getType(): Class<T>

    /**
     * An interface that may be used in an [activity][Activity] to provide [view processors][CyaneaViewProcessor]
     * to the [CyaneaDelegate].
     */
    interface Provider {

        /**
         * Get an array of [view processors][CyaneaViewProcessor] to style views.
         *
         * @return An array of decorators for the [CyaneaDelegate].
         */
        fun getViewProcessors(): Array<CyaneaViewProcessor<out View>>

    }

}

// ================================================================================================
// Processors

internal class AlertDialogProcessor : CyaneaViewProcessor<View>() {

    override fun getType(): Class<View> = View::class.java

    override fun shouldProcessView(view: View): Boolean = view is AlertDialogLayout || CLASS_NAME == view.javaClass.name

    override fun process(view: View, attrs: AttributeSet?, cyanea: Cyanea) {
        view.setBackgroundColor(cyanea.backgroundColor) // Theme AlertDialog background
    }

    companion object {
        private const val CLASS_NAME = "com.android.internal.widget.AlertDialogLayout"
    }

}

internal class BottomAppBarProcessor : CyaneaViewProcessor<BottomAppBar>() {

    override fun getType(): Class<BottomAppBar> = BottomAppBar::class.java

    override fun process(view: BottomAppBar, attrs: AttributeSet?, cyanea: Cyanea) {
        view.backgroundTint?.let { view.backgroundTint = cyanea.tinter.tint(it) }
        view.post {
            view.context?.let { context ->
                (context as? Activity)?.run {
                    cyanea.tint(view.menu, this)
                } ?: ((context as? ContextWrapper)?.baseContext as? Activity)?.run {
                    cyanea.tint(view.menu, this)
                }
            }
        }
    }

}

/**
 * A [CyaneaViewProcessor] that styles [buttons][CompoundButton] in the overflow menu.
 */
@RequiresApi(Build.VERSION_CODES.M)
internal class CompoundButtonProcessor : CyaneaViewProcessor<CompoundButton>() {

    override fun getType(): Class<CompoundButton> = CompoundButton::class.java

    @SuppressLint("PrivateResource")
    override fun process(view: CompoundButton, attrs: AttributeSet?, cyanea: Cyanea) {
        view.buttonTintList?.let { cyanea.tinter.tint(it) } ?: run {
            view.buttonTintList = cyanea.tinter.tint(
                    view.context.getColorStateList(R.color.abc_tint_btn_checkable)
            )
        }
        val background = view.background
        if (background is RippleDrawable) {
            val resid = if (cyanea.isDark) R.color.ripple_material_dark else R.color.ripple_material_light
            val unchecked = ContextCompat.getColor(view.context, resid)
            val checked = ColorUtils.adjustAlpha(cyanea.accent, 0.4f)
            val csl = ColorStateList(
                    arrayOf(
                            intArrayOf(-attr.state_activated, -attr.state_checked),
                            intArrayOf(attr.state_activated),
                            intArrayOf(attr.state_checked)
                    ),
                    intArrayOf(unchecked, checked, checked)
            )
            background.setColor(csl)
        }
    }

}

internal class DatePickerProcessor : CyaneaViewProcessor<DatePicker>() {

    override fun getType(): Class<DatePicker> = DatePicker::class.java

    override fun process(view: DatePicker, attrs: AttributeSet?, cyanea: Cyanea) {
        val datePickerId = view.context.resources.getIdentifier("date_picker_header", "id", "android")
        if (datePickerId != 0) {
            view.findViewById<ViewGroup>(datePickerId)?.let { layout ->
                cyanea.tinter.tint(layout.background)
                if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                    cyanea.tinter.tint(layout.backgroundTintList)
                }
            }
        }
    }

}

internal class FloatingActionButtonProcessor : CyaneaViewProcessor<FloatingActionButton>() {

    override fun getType(): Class<FloatingActionButton> = FloatingActionButton::class.java

    override fun process(view: FloatingActionButton, attrs: AttributeSet?, cyanea: Cyanea) {
        cyanea.tinter.tint(view.backgroundTintList)
    }

}

@TargetApi(Build.VERSION_CODES.M)
internal class ImageButtonProcessor : CyaneaViewProcessor<ImageButton>() {

    override fun getType(): Class<ImageButton> = ImageButton::class.java

    override fun process(view: ImageButton, attrs: AttributeSet?, cyanea: Cyanea) {
        cyanea.tinter.tint(view.background)
    }

}

/**
 * Style menu items
 */
internal class ListMenuItemViewProcessor : CyaneaViewProcessor<View>() {

    override fun getType(): Class<View> = View::class.java

    override fun shouldProcessView(view: View): Boolean = view is ListMenuItemView || view.javaClass.name == CLASS_NAME

    override fun process(view: View, attrs: AttributeSet?, cyanea: Cyanea) {
        cyanea.tinter.tint(view)
    }

    companion object {
        private const val CLASS_NAME = "com.android.internal.view.menu.ListMenuItemView"
    }

}

internal class NavigationViewProcessor : CyaneaViewProcessor<NavigationView>() {

    override fun getType(): Class<NavigationView> = NavigationView::class.java

    override fun process(view: NavigationView, attrs: AttributeSet?, cyanea: Cyanea) {
        val baseColor = if (cyanea.isDark) Color.WHITE else Color.BLACK
        val unselectedTextColor = ColorUtils.adjustAlpha(baseColor, 0.87f)
        val unselectedIconColor = ColorUtils.adjustAlpha(baseColor, 0.54f)
        val checkedColor = cyanea.accent

        view.apply {
            itemTextColor = ColorStateList(
                    arrayOf(
                            intArrayOf(-android.R.attr.state_checked),
                            intArrayOf(android.R.attr.state_checked)
                    ),
                    intArrayOf(unselectedTextColor, checkedColor)
            )
            itemIconTintList = ColorStateList(
                    arrayOf(
                            intArrayOf(-android.R.attr.state_checked),
                            intArrayOf(android.R.attr.state_checked)
                    ),
                    intArrayOf(unselectedIconColor, checkedColor)
            )
        }
    }

}

@TargetApi(Build.VERSION_CODES.M)
internal class SearchAutoCompleteProcessor : CyaneaViewProcessor<SearchAutoComplete>() {

    override fun getType(): Class<SearchAutoComplete> = SearchAutoComplete::class.java

    override fun process(view: SearchAutoComplete, attrs: AttributeSet?, cyanea: Cyanea) {
        WidgetTint.setCursorColor(view, cyanea.accent)
    }

}

@TargetApi(Build.VERSION_CODES.M)
internal class SwitchProcessor : CyaneaViewProcessor<Switch>() {

    override fun getType(): Class<Switch> = Switch::class.java

    @SuppressLint("PrivateResource")
    override fun process(view: Switch, attrs: AttributeSet?, cyanea: Cyanea) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            cyanea.tinter.tint(view.thumbDrawable)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.trackTintList = ContextCompat.getColorStateList(view.context, R.color.abc_tint_switch_track)
        }
    }

}

internal class SwitchCompatProcessor : CyaneaViewProcessor<SwitchCompat>() {

    override fun getType(): Class<SwitchCompat> = SwitchCompat::class.java

    @SuppressLint("RestrictedApi", "PrivateResource")
    override fun process(view: SwitchCompat, attrs: AttributeSet?, cyanea: Cyanea) {
        // SwitchCompat sets a ColorStateList on the drawable. Here, we get and modify the tint.
        val manager = AppCompatDrawableManager.get()
        Reflection.invoke<ColorStateList>(manager, "getTintList",
                arrayOf(Context::class.java, Int::class.java),
                view.context,
                androidx.appcompat.R.drawable.abc_switch_thumb_material
        )?.let { csl ->
            cyanea.tinter.tint(csl)
        }
    }

}

internal class TextInputLayoutProcessor : CyaneaViewProcessor<TextInputLayout>() {

    override fun getType(): Class<TextInputLayout> = TextInputLayout::class.java

    override fun process(view: TextInputLayout, attrs: AttributeSet?, cyanea: Cyanea) {
        if (view.boxStrokeColor == Cyanea.getOriginalColor(R.color.cyanea_accent_reference)) {
            view.boxStrokeColor = cyanea.accent
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Reflection.getFieldValue<ColorStateList?>(view, "focusedTextColor")?.let { csl ->
                cyanea.tinter.tint(csl)
            }
        }
    }

}

internal class TextViewProcessor : CyaneaViewProcessor<TextView>() {

    override fun getType(): Class<TextView> = TextView::class.java

    override fun process(view: TextView, attrs: AttributeSet?, cyanea: Cyanea) {
        view.textColors?.let { colors ->
            view.setTextColor(cyanea.tinter.tint(colors))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cyanea.tinter.tint(view.backgroundTintList)
        }
        cyanea.tinter.tint(view.background)
    }

}

internal class TimePickerProcessor : CyaneaViewProcessor<TimePicker>() {

    override fun getType(): Class<TimePicker> = TimePicker::class.java

    override fun process(view: TimePicker, attrs: AttributeSet?, cyanea: Cyanea) {
        cyanea.tinter.tint(view)
    }

}

@TargetApi(Build.VERSION_CODES.M)
internal class ViewGroupProcessor : CyaneaViewProcessor<ViewGroup>() {

    override fun getType(): Class<ViewGroup> = ViewGroup::class.java

    override fun process(view: ViewGroup, attrs: AttributeSet?, cyanea: Cyanea) {
        EdgeEffectTint.setEdgeGlowColor(view, cyanea.primary)
        cyanea.tinter.tint(view.background)
        if (view is AbsListView) {
            WidgetTint.setFastScrollThumbColor(view, cyanea.accent)
        }
    }

}
