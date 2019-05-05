package com.jorkoh.transportezaragozakt.destinations.reminders

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.*
import kotlinx.android.synthetic.main.reminder_row.view.*

class RemindersAdapter(
    private val edit: (ReminderExtended) -> Unit,
    private val editAlias: (ReminderExtended) -> Unit,
    private val editColor: (ReminderExtended) -> Unit,
    private val restore: (ReminderExtended) -> Unit,
    private val reorder: (RecyclerView.ViewHolder) -> Unit,
    private val delete: (ReminderExtended) -> Unit
) : RecyclerView.Adapter<RemindersAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            reminder: ReminderExtended,
            edit: (ReminderExtended) -> Unit,
            editAlias: (ReminderExtended) -> Unit,
            editColor: (ReminderExtended) -> Unit,
            restore: (ReminderExtended) -> Unit,
            reorder: (RecyclerView.ViewHolder) -> Unit,
            delete: (ReminderExtended) -> Unit
        ) {
            itemView.apply {
                @SuppressLint("SetTextI18n")
                reminder_time_text.text = "${"%02d".format(reminder.hourOfDay)}:${"%02d".format(reminder.minute)}"

                reminder_monday.isChecked = reminder.daysOfWeek[0]
                reminder_tuesday.isChecked = reminder.daysOfWeek[1]
                reminder_wednesday.isChecked = reminder.daysOfWeek[2]
                reminder_thursday.isChecked = reminder.daysOfWeek[3]
                reminder_friday.isChecked = reminder.daysOfWeek[4]
                reminder_saturday.isChecked = reminder.daysOfWeek[5]
                reminder_sunday.isChecked = reminder.daysOfWeek[6]

                type_image_reminder.setImageResource(
                    when (reminder.type) {
                        StopType.BUS -> R.drawable.ic_bus
                        StopType.TRAM -> R.drawable.ic_tram
                    }
                )
                title_text_reminder.text = reminder.alias
                if (reminder.colorHex.isNotEmpty()) {
                    reminder_color.setBackgroundColor(Color.parseColor(reminder.colorHex))
                    reminder_color.visibility = View.VISIBLE
                } else {
                    reminder_color.setBackgroundColor(Color.TRANSPARENT)
                    reminder_color.visibility = View.GONE
                }

                itemView.lines_layout.removeAllViews()
                val layoutInflater = LayoutInflater.from(context)
                reminder.lines.forEachIndexed { index, line ->
                    layoutInflater.inflate(R.layout.map_info_window_line, itemView.lines_layout)
                    val lineView = itemView.lines_layout.getChildAt(index) as TextView

                    val lineColor = if (reminder.type == StopType.BUS) R.color.bus_color else R.color.tram_color
                    lineView.background.setColorFilter(
                        ContextCompat.getColor(context, lineColor),
                        PorterDuff.Mode.SRC_IN
                    )
                    lineView.text = line
                }

                setOnClickListener { edit(reminder) }
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
    }

    lateinit var reminders: List<ReminderExtended>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reminder_row, parent, false) as View
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(reminders[position], edit, editAlias, editColor, restore, reorder, delete)
    }

    override fun getItemCount(): Int = if (::reminders.isInitialized) reminders.size else 0

    fun setNewReminders(newReminders: List<ReminderExtended>) {
        if (::reminders.isInitialized) {
            if (isOnlyPositionChange(newReminders)) {
                reminders = newReminders
            } else {
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
        } else {
            reminders = newReminders
            notifyItemRangeInserted(0, reminders.size)
        }
    }

    private fun isOnlyPositionChange(newReminders: List<ReminderExtended>) =
        newReminders.count() == reminders.count() && newReminders.containsAll(reminders)
}