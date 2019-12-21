package com.jorkoh.transportezaragozakt.destinations.more

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.IdRes
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.destinations.utils.lighter


class ThemePickerAdapter(private val themes: List<ThemePickerFragment.CustomTheme>, private val context: Context) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder

        holder = if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.theme_item, parent, false)
            ViewHolder(view)
        } else {
            convertView.tag as ViewHolder
        }
        val theme = themes[position]

        holder.find<FrameLayout>(R.id.preview).setBackgroundColor(theme.backgroundColor)
        holder.find<LinearLayout>(R.id.action_bar_panel).setBackgroundColor(theme.primaryColor)

        val fab = holder.find<FloatingActionButton>(R.id.fab_add)
        // Bug: when using the accent color, the last selected item's FAB is the same color as the newly selected FAB.
        // For now, slightly adjust the color.
        val fabMainColor = lighter(theme.secondaryColor, 0.01f)
        fab.backgroundTintList = ColorStateList.valueOf(fabMainColor)
        fab.supportBackgroundTintList = ColorStateList.valueOf(fabMainColor)

        val title = holder.find<TextView>(R.id.title)
        title.text = theme.name

        if (theme.isMatchingColorScheme(context)) {
            title.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.colorThemeSelected))
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.theme_check, 0)
        } else {
            title.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.colorThemeNotSelected))
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        val isDark = ColorUtils.calculateLuminance(theme.primaryColor) <= 0.75
        val menuIconColor = if (isDark) Color.WHITE else Color.BLACK
        val drawable = DrawerArrowDrawable(parent.context)
        drawable.color = menuIconColor

        val drawer = holder.find<ImageView>(R.id.material_drawer_drawable)
        val overflow = holder.find<ImageView>(R.id.action_overflow)
        drawer.setImageDrawable(drawable)
        overflow.setColorFilter(menuIconColor, PorterDuff.Mode.SRC_ATOP)

        return holder.itemView
    }

    override fun getItem(position: Int): ThemePickerFragment.CustomTheme = themes[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = themes.size

    private class ViewHolder(val itemView: View) {

        private val views = SparseArray<View>()

        init {
            itemView.tag = this
        }

        fun <T : View> find(@IdRes id: Int): T {
            views[id]?.let {
                @Suppress("UNCHECKED_CAST")
                return it as T
            } ?: run {
                val view = itemView.findViewById<T>(id)
                views.put(id, view)
                return view
            }
        }

    }

}