package com.jorkoh.transportezaragozakt.destinations.reminders

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.ReminderExtended
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.destinations.getOnBackgroundColor
import com.jorkoh.transportezaragozakt.destinations.inflateLines
import com.jorkoh.transportezaragozakt.destinations.setDrawableColor
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.reminder_row.*
import kotlinx.android.synthetic.main.reminder_row.view.*

class RemindersAdapter(
    private val edit: (ReminderExtended) -> Unit,
    private val editAlias: (ReminderExtended) -> Unit,
    private val editColor: (ReminderExtended) -> Unit,
    private val restore: (ReminderExtended) -> Unit,
    private val reorder: (RecyclerView.ViewHolder) -> Unit,
    private val delete: (ReminderExtended, Int) -> Unit
) : RecyclerView.Adapter<RemindersAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        val context: Context
            get() = itemView.context

        fun bind(
            reminder: ReminderExtended,
            edit: (ReminderExtended) -> Unit,
            editAlias: (ReminderExtended) -> Unit,
            editColor: (ReminderExtended) -> Unit,
            restore: (ReminderExtended) -> Unit,
            reorder: (RecyclerView.ViewHolder) -> Unit,
            delete: (ReminderExtended, Int) -> Unit
        ) {
            // Stop type icon
            when (reminder.type) {
                StopType.BUS -> {
                    type_image_reminder.setImageResource(R.drawable.ic_bus_stop)
                    type_image_reminder.contentDescription = context.getString(R.string.stop_type_bus)
                }
                StopType.TRAM -> {
                    type_image_reminder.setImageResource(R.drawable.ic_tram_stop)
                    type_image_reminder.contentDescription = context.getString(R.string.stop_type_tram)
                }
                StopType.RURAL -> {
                    type_image_reminder.setImageResource(R.drawable.ic_rural_stop)
                    type_image_reminder.contentDescription = context.getString(R.string.stop_type_rural)
                }
            }
            // Days checkboxes
            reminder_monday.isChecked = reminder.daysOfWeek.days[0]
            reminder_tuesday.isChecked = reminder.daysOfWeek.days[1]
            reminder_wednesday.isChecked = reminder.daysOfWeek.days[2]
            reminder_thursday.isChecked = reminder.daysOfWeek.days[3]
            reminder_friday.isChecked = reminder.daysOfWeek.days[4]
            reminder_saturday.isChecked = reminder.daysOfWeek.days[5]
            reminder_sunday.isChecked = reminder.daysOfWeek.days[6]

            val color = getOnBackgroundColor(context)
            reminder_monday.setDrawableColor(color)
            reminder_tuesday.setDrawableColor(color)
            reminder_wednesday.setDrawableColor(color)
            reminder_thursday.setDrawableColor(color)
            reminder_friday.setDrawableColor(color)
            reminder_saturday.setDrawableColor(color)
            reminder_sunday.setDrawableColor(color)
            // Texts
            @SuppressLint("SetTextI18n")
            val timeString = "${"%02d".format(reminder.hourOfDay)}:${"%02d".format(reminder.minute)}"
            reminder_time_text.text = timeString
            reminder_time_text.contentDescription = context.getString(R.string.time_template, timeString)
            // Reminder user defined color
            title_text_reminder.text = reminder.alias
            if (reminder.colorHex.isNotEmpty()) {
                reminder_color.setBackgroundColor(Color.parseColor(reminder.colorHex))
                reminder_color.visibility = View.VISIBLE
            } else {
                reminder_color.setBackgroundColor(Color.TRANSPARENT)
                reminder_color.visibility = View.GONE
            }
            // Lines
            reminder.lines.inflateLines(itemView.lines_layout_favorite, reminder.type, context)
            // Listeners
            itemView.setOnClickListener { edit(reminder) }
            edit_view_reminder.setOnClickListener {
                PopupMenu(context, it).apply {
                    menu.apply {
                        add(context.resources.getString(R.string.reminder)).setOnMenuItemClickListener {
                            edit(reminder)
                            true
                        }
                        add(context.resources.getString(R.string.alias)).setOnMenuItemClickListener {
                            editAlias(reminder)
                            true
                        }
                        add(context.resources.getString(R.string.color)).setOnMenuItemClickListener {
                            editColor(reminder)
                            true
                        }
                        add(context.resources.getString(R.string.restore)).setOnMenuItemClickListener {
                            restore(reminder)
                            true
                        }
                        add(context.resources.getString(R.string.delete)).setOnMenuItemClickListener {
                            delete(reminder, adapterPosition)
                            true
                        }
                    }
                    show()
                }
            }
            reorder_view_reminder.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    reorder(this@ReminderViewHolder)
                }
                return@setOnTouchListener true
            }
        }
    }

    var reminders: List<ReminderExtended> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reminder_row, parent, false) as View
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(reminders[position], edit, editAlias, editColor, restore, reorder, delete)
    }

    override fun getItemCount(): Int =reminders.size

    fun setNewReminders(newReminders: List<ReminderExtended>) {
        if (isOnlyPositionChange(newReminders)) {
            // In the case of drag and drop positional changes the reordering has already taken place visually
            reminders = newReminders
        } else {
            // In any other case the difference is calculated with DiffUtil
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = reminders.size

                override fun getNewListSize() = newReminders.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return reminders[oldItemPosition].reminderId == newReminders[newItemPosition].reminderId
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return reminders[oldItemPosition].daysOfWeek == newReminders[newItemPosition].daysOfWeek
                            && reminders[oldItemPosition].hourOfDay == newReminders[newItemPosition].hourOfDay
                            && reminders[oldItemPosition].minute == newReminders[newItemPosition].minute
                            && reminders[oldItemPosition].alias == newReminders[newItemPosition].alias
                            && reminders[oldItemPosition].colorHex == newReminders[newItemPosition].colorHex
                            && reminders[oldItemPosition].lines == newReminders[newItemPosition].lines
                            && reminders[oldItemPosition].type == newReminders[newItemPosition].type
                }
            })
            reminders = newReminders
            result.dispatchUpdatesTo(this)
        }
    }

    private fun isOnlyPositionChange(newReminders: List<ReminderExtended>) =
        newReminders.count() == reminders.count() && newReminders.containsAll(reminders)
}