package com.jorkoh.transportezaragozakt.destinations.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Line
import com.jorkoh.transportezaragozakt.db.LineType
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsFragment
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.destinations.utils.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.utils.isSpanish
import com.jorkoh.transportezaragozakt.destinations.utils.toPx
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.line_row.*

private const val STATE_LAST_SELECTED_ID = "last_selected_id"

// Used to display lines on LinesFragment RecyclerView
class LineAdapter(
    private val openLine: (LineDetailsFragmentArgs, Array<Pair<View, String>>) -> Unit,
    private val onReadyToTransition: () -> Unit
) : RecyclerView.Adapter<LineAdapter.LineViewHolder>(), Filterable {

    inner class LineViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        val context: Context
            get() = itemView.context

        fun bind(
            line: Line,
            openLine: (LineDetailsFragmentArgs, Array<Pair<View, String>>) -> Unit
        ) {
            // Line type color
            line_row_title.setBackgroundColor(
                ContextCompat.getColor(
                    context, when (line.type) {
                        LineType.BUS -> R.color.bus_color
                        LineType.TRAM -> R.color.tram_color
                        LineType.RURAL -> R.color.rural_color
                    }
                )
            )
            line_row_title.minWidth = when (line.type) {
                LineType.BUS -> 60.toPx()
                LineType.TRAM -> 60.toPx()
                LineType.RURAL -> 81.toPx()
            }
            // Texts
            line_row_title.text = if (context.isSpanish()) line.nameES else line.nameEN
            line_row_first_destination.text = line.destinations[0]
            if (line.destinations.count() > 1) {
                line_row_second_destination.text = line.destinations[1]
                line_row_second_destination.visibility = View.VISIBLE
            } else {
                line_row_second_destination.visibility = View.GONE
            }
            // Listeners
            itemView.setOnClickListener(DebounceClickListener {
                // Record the selected item so that we can make the item ready before starting the reenter transition.
                lastSelectedId = line.lineId

                openLine(
                    LineDetailsFragmentArgs(line.type.name, line.lineId, null),
                    arrayOf(
                        line_row_card to LineDetailsFragment.TRANSITION_NAME_BACKGROUND,
                        line_row_mirror_body to LineDetailsFragment.TRANSITION_NAME_BODY_DETAILS,
                        line_row_constraint_layout to LineDetailsFragment.TRANSITION_NAME_BODY_ROW
                    )
                )
            })
        }
    }

    private var lastSelectedId: String? = null

    val expectsTransition: Boolean
        get() = lastSelectedId != null

    private var displayedLines: List<Line> = listOf()
    private var linesFull: List<Line> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        return LineViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.line_row, parent, false))
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        val line = displayedLines[position]

        ViewCompat.setTransitionName(holder.line_row_card, "line_row_card_${line.lineId}")
        ViewCompat.setTransitionName(holder.line_row_mirror_body, "line_row_mirror_body_${line.lineId}")
        ViewCompat.setTransitionName(holder.line_row_constraint_layout, "line_row_constraint_layout_${line.lineId}")

        holder.bind(line, openLine)

        if (line.lineId == lastSelectedId) {
            onReadyToTransition()
            lastSelectedId = null
        }
    }

    fun saveInstanceState(outState: Bundle) {
        lastSelectedId?.let { id ->
            outState.putString(STATE_LAST_SELECTED_ID, id)
        }
    }

    fun restoreInstanceState(state: Bundle) {
        if (lastSelectedId == null && state.containsKey(STATE_LAST_SELECTED_ID)) {
            lastSelectedId = state.getString(STATE_LAST_SELECTED_ID)
        }
    }

    override fun getItemCount(): Int = displayedLines.size

    fun setNewLines(newLines: List<Line>, query: String?) {
        linesFull = newLines
        filter.filter(query)
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults = FilterResults().apply {
            values = if (constraint.isNullOrEmpty()) {
                linesFull
            } else {
                val filterPattern = constraint.toString().trim()
                linesFull.filter {
                    // Filtering by name and destinations
                    (it.nameES + it.nameEN + it.destinations[0] + it.destinations.getOrElse(1) { "" }).contains(
                        filterPattern,
                        ignoreCase = true
                    )
                }
            }
            @Suppress("UNCHECKED_CAST")
            // Flag to control whether the recycler view should scroll to the top
            count = if ((values as List<Stop>).count() != displayedLines.count()) 1 else 0
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            @Suppress("UNCHECKED_CAST")
            val filteredLines = results?.values as List<Line>

            // Using DiffUtil to make the filtering feel more dynamic
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = displayedLines.size

                override fun getNewListSize() = filteredLines.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return displayedLines[oldItemPosition].lineId == filteredLines[newItemPosition].lineId
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return displayedLines[oldItemPosition].type == filteredLines[newItemPosition].type
                            && displayedLines[oldItemPosition].nameES == filteredLines[newItemPosition].nameES
                            && displayedLines[oldItemPosition].nameEN == filteredLines[newItemPosition].nameEN
                            && displayedLines[oldItemPosition].destinations == filteredLines[newItemPosition].destinations
                }
            })
            displayedLines = filteredLines
            result.dispatchUpdatesTo(this@LineAdapter)
        }
    }
}