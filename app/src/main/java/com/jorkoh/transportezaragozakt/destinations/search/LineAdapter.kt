package com.jorkoh.transportezaragozakt.destinations.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Line
import com.jorkoh.transportezaragozakt.db.LineType
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.destinations.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsFragmentArgs
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.line_row.*

// Used to display lines on LinesFragment RecyclerView
class LineAdapter(
    private val openLine: (LineDetailsFragmentArgs) -> Unit
) : RecyclerView.Adapter<LineAdapter.LineViewHolder>(), Filterable {

    class LineViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        val context: Context
            get() = itemView.context

        fun bind(
            line: Line,
            openLine: (LineDetailsFragmentArgs) -> Unit
        ) {
            // Line type color
            line_text_line.setBackgroundColor(
                ContextCompat.getColor(
                    context, when (line.type) {
                        LineType.BUS -> R.color.bus_color
                        LineType.TRAM -> R.color.tram_color
                    }
                )
            )
            // Texts
            line_text_line.text = line.name
            first_destination_line.text = line.destinations[0]
            if (line.destinations.count() > 1) {
                second_destination_line.text = line.destinations[1]
                second_destination_line.visibility = View.VISIBLE
            } else {
                second_destination_line.visibility = View.GONE
            }
            // Listeners
            itemView.setOnClickListener(DebounceClickListener {
                openLine(LineDetailsFragmentArgs(line.type.name, line.lineId, null))
            })
        }
    }

    private var displayedLines: List<Line> = listOf()
    private var linesFull: List<Line> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.line_row, parent, false)
        return LineViewHolder(view)
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        holder.bind(displayedLines[position], openLine)
    }

    override fun getItemCount(): Int = displayedLines.size

    // When setting new stops we need to call filter afterwards to see the effects
    fun setNewLines(newLines: List<Line>) {
        linesFull = newLines
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults = FilterResults().apply {
            values = if (constraint.isNullOrEmpty()) {
                linesFull
            } else {
                val filterPattern = constraint.toString().trim()
                linesFull.filter {
                    // Filtering by name and destinations
                    (it.name + it.destinations[0] + it.destinations.getOrElse(1) { "" }).contains(
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
                            && displayedLines[oldItemPosition].name == filteredLines[newItemPosition].name
                            && displayedLines[oldItemPosition].destinations == filteredLines[newItemPosition].destinations
                }
            })
            displayedLines = filteredLines
            result.dispatchUpdatesTo(this@LineAdapter)
        }
    }
}