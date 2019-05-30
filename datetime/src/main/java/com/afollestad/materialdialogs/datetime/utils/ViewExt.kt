/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("DEPRECATION")

package com.afollestad.materialdialogs.datetime.utils

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.util.TypedValue
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat.getColor
import androidx.viewpager.widget.ViewPager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.R
import com.afollestad.viewpagerdots.DotsIndicator


internal fun TimePicker.hour(): Int = if (isNougat()) hour else currentHour

internal fun TimePicker.minute(): Int = if (isNougat()) minute else currentMinute

internal fun TimePicker.hour(value: Int) {
    if (isNougat()) hour = value else currentHour = value
}

internal fun TimePicker.minute(value: Int) {
    if (isNougat()) minute = value else currentMinute = value
}

internal fun MaterialDialog.getDatePicker() = findViewById<DatePicker>(R.id.datetimeDatePicker)

internal fun MaterialDialog.getTimePicker() = findViewById<TimePicker>(R.id.datetimeTimePicker)

internal fun MaterialDialog.getPager() = findViewById<ViewPager>(R.id.dateTimePickerPager)

internal fun MaterialDialog.setDaysOfWeek(daysOfWeek: List<Boolean>) {
    findViewById<CheckBox>(R.id.monday).isChecked = daysOfWeek[0]
    findViewById<CheckBox>(R.id.tuesday).isChecked = daysOfWeek[1]
    findViewById<CheckBox>(R.id.wednesday).isChecked = daysOfWeek[2]
    findViewById<CheckBox>(R.id.thursday).isChecked = daysOfWeek[3]
    findViewById<CheckBox>(R.id.friday).isChecked = daysOfWeek[4]
    findViewById<CheckBox>(R.id.saturday).isChecked = daysOfWeek[5]
    findViewById<CheckBox>(R.id.sunday).isChecked = daysOfWeek[6]
}

internal fun MaterialDialog.getDaysOfWeek(): List<Boolean> {
    return listOf(
        findViewById<CheckBox>(R.id.monday).isChecked,
        findViewById<CheckBox>(R.id.tuesday).isChecked,
        findViewById<CheckBox>(R.id.wednesday).isChecked,
        findViewById<CheckBox>(R.id.thursday).isChecked,
        findViewById<CheckBox>(R.id.friday).isChecked,
        findViewById<CheckBox>(R.id.saturday).isChecked,
        findViewById<CheckBox>(R.id.sunday).isChecked
    )
}

internal fun MaterialDialog.getPageIndicator() =
    findViewById<DotsIndicator?>(R.id.datetimePickerPagerDots)

private fun isNougat() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

internal fun MaterialDialog.tintCheckBoxes() {
    val color = getOnBackgroundColor(context)

    findViewById<CheckBox>(R.id.monday).setDrawableColor(color)
    findViewById<CheckBox>(R.id.tuesday).setDrawableColor(color)
    findViewById<CheckBox>(R.id.wednesday).setDrawableColor(color)
    findViewById<CheckBox>(R.id.thursday).setDrawableColor(color)
    findViewById<CheckBox>(R.id.friday).setDrawableColor(color)
    findViewById<CheckBox>(R.id.saturday).setDrawableColor(color)
    findViewById<CheckBox>(R.id.sunday).setDrawableColor(color)
}

internal fun getOnBackgroundColor(context: Context): Int {
    val typedValue = TypedValue()

    val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorOnBackground))
    val color = a.getColor(0, 0)

    a.recycle()

    return color
}

internal fun CheckBox.setDrawableColor(@ColorRes color: Int) {
    compoundDrawables.filterNotNull().forEach {
        it.colorFilter = PorterDuffColorFilter(getColor(context, color), PorterDuff.Mode.SRC_IN)
    }
}
