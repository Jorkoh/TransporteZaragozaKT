package com.jorkoh.transportezaragozakt.destinations.search

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.location.Location
import android.view.*
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.destinations.DebounceClickListener
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import kotlinx.android.synthetic.main.line_row.view.*
import kotlinx.android.synthetic.main.stop_row.view.*

class LineAdapter(
    private val openLine: (LineDetailsFragmentArgs) -> Unit
) : RecyclerView.Adapter<LineAdapter.LineViewHolder>(), Filterable {

    class LineViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            line: Line,
            openLine: (LineDetailsFragmentArgs) -> Unit
        ) {
            itemView.apply {
                line_text_line.setBackgroundColor(
                    ContextCompat.getColor(
                        context, when (line.type) {
                            LineType.BUS -> R.color.bus_color
                            LineType.TRAM -> R.color.tram_color
                        }
                    )
                )

                line_text_line.text = line.name

                first_destination_line.text = line.destinations[0]
                if (line.destinations.count() > 1) {
                    second_destination_line.text = line.destinations[1]
                    second_destination_line.visibility = View.VISIBLE
                } else {
                    second_destination_line.visibility = View.GONE
                }

                setOnClickListener(DebounceClickListener {
                    openLine(LineDetailsFragmentArgs(line.type.name, line.lineId))
                })
            }
        }
    }

    private var displayedLines: List<Line> = listOf()
    private var linesFull: List<Line> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.line_row, parent, false) as View
        return LineViewHolder(view)
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        holder.bind(displayedLines[position], openLine)
    }

    override fun getItemCount(): Int = displayedLines.size

    //When setting new stops we need to call filter afterwards to see the effects
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
                    (it.name + it.destinations[0] + it.destinations.getOrElse(1) { "" }).contains(
                        filterPattern,
                        ignoreCase = true
                    )
                }
            }
            @Suppress("UNCHECKED_CAST")
            //Flag to control wheter the recycler view should scroll to the top
            count = if ((values as List<Stop>).count() != displayedLines.count()) 1 else 0
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            @Suppress("UNCHECKED_CAST")
            val filteredLines = results?.values as List<Line>

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