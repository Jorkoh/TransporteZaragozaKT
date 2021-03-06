package com.jorkoh.transportezaragozakt.destinations.reminders

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.input.input
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.ReminderExtended
import com.jorkoh.transportezaragozakt.destinations.utils.FragmentWithToolbar
import com.jorkoh.transportezaragozakt.destinations.favorites.ItemGestureHelper
import com.jorkoh.transportezaragozakt.destinations.utils.materialColors
import com.jorkoh.transportezaragozakt.destinations.utils.toColorFromHex
import com.jorkoh.transportezaragozakt.destinations.utils.toHexFromColor
import kotlinx.android.synthetic.main.reminders_destination.*
import kotlinx.android.synthetic.main.reminders_destination.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class RemindersFragment : FragmentWithToolbar() {

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

            override fun onItemSwiped(position: Int) {
                delete(remindersAdapter.reminders[position], position)
            }
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
            timePicker(
                show24HoursView = DateFormat.is24HourFormat(requireContext()),
                currentTime = time,
                daysOfWeek = reminder.daysOfWeek.days
            ) { _, time, daysOfWeek ->
                remindersVM.updateReminder(reminder.reminderId, daysOfWeek, time)
            }
            positiveButton(R.string.edit)
        }
    }

    private val editAlias: (ReminderExtended) -> Unit = { reminder ->
        MaterialDialog(requireContext())
            .show {
                title(R.string.edit_reminder_dialog_title)
                input(prefill = reminder.alias) { _, newAlias ->
                    remindersVM.updateReminder(reminder.reminderId, newAlias.toString(), reminder.colorHex)
                }
                positiveButton(R.string.edit)
            }
    }

    private val editColor: (ReminderExtended) -> Unit = { reminder ->
        MaterialDialog(requireContext()).show {
            title(R.string.edit_reminder_dialog_title)
            colorChooser(materialColors, initialSelection = reminder.colorHex.toColorFromHex()) { _, color ->
                remindersVM.updateReminder(reminder.reminderId, reminder.alias, color.toHexFromColor())
            }
            positiveButton(R.string.edit)
        }
    }

    private val restore: (ReminderExtended) -> Unit = { reminder ->
        MaterialDialog(requireContext()).show {
            title(R.string.restore_reminder_title)
            message(R.string.restore_reminder_message)
            positiveButton(R.string.restore) {
                remindersVM.restoreReminder(reminder)
            }
            negativeButton(R.string.cancel)
        }
    }

    private val reorder: (RecyclerView.ViewHolder) -> Unit = { viewHolder ->
        itemTouchHelper.startDrag(viewHolder)
    }

    private val delete: (ReminderExtended, Int) -> Unit = { reminder, position ->
        MaterialDialog(requireContext()).show {
            title(R.string.delete_reminder_title)
            message(R.string.delete_reminder_message)
            positiveButton(R.string.delete) {
                remindersVM.deleteReminder(reminder.reminderId)
            }
            negativeButton(R.string.cancel) {
                remindersAdapter.notifyItemChanged(position)
            }
        }.onCancel {
            remindersAdapter.notifyItemChanged(position)
        }
    }

    private val remindersAdapter: RemindersAdapter = RemindersAdapter(edit, editAlias, editColor, restore, reorder, delete)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        remindersVM.reminders.observe(viewLifecycleOwner, Observer { reminders ->
            reminders?.let {
                updateEmptyViewVisibility(reminders.isEmpty(), view)
                remindersAdapter.setNewReminders(reminders)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.reminders_destination, container, false)
        rootView.reminders_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = remindersAdapter
        }
        itemTouchHelper.attachToRecyclerView(rootView.reminders_recycler_view)

        return rootView
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

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid leaks
        reminders_recycler_view?.adapter = null
        itemTouchHelper.attachToRecyclerView(null)
    }
}
