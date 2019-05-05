package com.jorkoh.transportezaragozakt.destinations.reminders

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.input.input
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.ReminderExtended
import com.jorkoh.transportezaragozakt.destinations.favorites.ItemGestureHelper
import com.jorkoh.transportezaragozakt.destinations.favorites.materialColors
import kotlinx.android.synthetic.main.reminders_destination.*
import kotlinx.android.synthetic.main.reminders_destination.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class RemindersFragment : Fragment() {

    private val remindersVM: RemindersViewModel by viewModel()

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = ItemGestureHelper(object : ItemGestureHelper.OnItemGestureListener {
            override fun onItemDrag(fromPosition: Int, toPosition: Int): Boolean {
                reminders_recycler_view.adapter?.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onItemDragged(fromPosition: Int, toPosition: Int) {
                remindersVM.moveReminder(fromPosition, toPosition)
            }

            override fun onItemSwiped(position: Int) {}
        })
        ItemTouchHelper(simpleItemTouchCallback)
    }

    private val edit: (ReminderExtended) -> Unit = { reminder ->
        val time = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hourOfDay)
            set(Calendar.MINUTE, reminder.minute)
        }

        MaterialDialog(requireContext()).show {
            title(R.string.edit_reminder_dialog_title)
            timePicker(show24HoursView = false, currentTime = time, daysOfWeek = reminder.daysOfWeek.days) { _, time, daysOfWeek ->
                remindersVM.updateReminder(reminder.reminderId, daysOfWeek, time)
            }
            positiveButton(R.string.edit_button)
        }
    }

    private val editAlias: (ReminderExtended) -> Unit = { reminder ->
        MaterialDialog(requireContext())
            .show {
                title(R.string.edit_reminder_dialog_title)
                input(prefill = reminder.alias) { _, newAlias ->
                    remindersVM.updateReminder(reminder.reminderId, newAlias.toString(), reminder.colorHex)
                }
                positiveButton(R.string.edit_button)
            }
    }

    private val editColor: (ReminderExtended) -> Unit = { reminder ->
        MaterialDialog(requireContext()).show {
            title(R.string.edit_reminder_dialog_title)
            colorChooser(
                materialColors,
                initialSelection = if (reminder.colorHex.isNullOrEmpty()) Color.TRANSPARENT else Color.parseColor(
                    reminder.colorHex
                )
            ) { _, color ->
                val hexColor = if (color == Color.TRANSPARENT) "" else String.format("#%06X", 0xFFFFFF and color)
                remindersVM.updateReminder(reminder.reminderId, reminder.alias, hexColor)
            }
            positiveButton(R.string.edit_button)
        }
    }

    private val restore: (ReminderExtended) -> Unit = { reminder ->
        MaterialDialog(requireContext()).show {
            title(R.string.restore_reminder_title)
            message(R.string.restore_reminder_message)
            positiveButton(R.string.restore) {
                remindersVM.restoreReminder(reminder.reminderId, reminder.stopId)
            }
            negativeButton(R.string.cancel)
        }
    }

    private val reorder: (RecyclerView.ViewHolder) -> Unit = { viewHolder ->
        itemTouchHelper.startDrag(viewHolder)
    }

    private val delete: (ReminderExtended) -> Unit = { reminder ->
    }

    private val remindersAdapter: RemindersAdapter =
        RemindersAdapter(edit, editAlias, editColor, restore, reorder, delete)

    private val remindersObserver = Observer<List<ReminderExtended>> { reminders ->
        reminders?.let {
            updateEmptyViewVisibility(reminders.isEmpty(), view)
            remindersAdapter.setNewReminders(reminders)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.reminders_destination, container, false)

        rootView.reminders_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = remindersAdapter
        }

        itemTouchHelper.attachToRecyclerView(rootView.reminders_recycler_view)

        remindersVM.reminders.observe(this, remindersObserver)
        updateEmptyViewVisibility(remindersVM.reminders.value.isNullOrEmpty(), rootView)
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        remindersVM.init()
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean, rootView: View?) {
        val newVisibility = if (isEmpty) {
            View.VISIBLE
        } else {
            View.GONE
        }
        rootView?.no_reminders_animation?.visibility = newVisibility
        rootView?.no_reminders_text?.visibility = newVisibility
    }
}
