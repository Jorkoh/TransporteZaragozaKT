package com.jorkoh.transportezaragozakt.destinations.favorites

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView

class ItemGestureHelper(private val listener: OnItemGestureListener) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(UP or DOWN or START or END, 0)
    }

    interface OnItemGestureListener {

        fun onItemDrag(fromPosition: Int, toPosition: Int): Boolean

        fun onItemDragged(fromPosition: Int, toPosition: Int)

        fun onItemSwiped(position: Int)
    }

    private var dragFromPosition = -1
    private var dragToPosition = -1

    // Other methods omitted...

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // Item is being dragged, keep the current target position
        dragToPosition = target.adapterPosition
        return listener.onItemDrag(viewHolder.adapterPosition, target.adapterPosition)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.onItemSwiped(viewHolder.adapterPosition)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                viewHolder?.also { dragFromPosition = it.adapterPosition }
                viewHolder?.itemView?.alpha = 0.65f
            }
            ItemTouchHelper.ACTION_STATE_IDLE -> {
                if (dragFromPosition != -1 && dragToPosition != -1 && dragFromPosition != dragToPosition) {
                    // Item successfully dragged
                    listener.onItemDragged(dragFromPosition, dragToPosition)
                    // Reset drag positions
                    dragFromPosition = -1
                    dragToPosition = -1
                }
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.alpha = 1.0f
    }
}