package com.jorkoh.transportezaragozakt.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jorkoh.transportezaragozakt.R

class StopDetailsAdapter(private val myDataset: MutableList<String>) :
    RecyclerView.Adapter<StopDetailsAdapter.StopDetailsViewHolder>() {

    class StopDetailsViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopDetailsViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.row_view, parent, false) as TextView
        return StopDetailsViewHolder(textView)
    }

    override fun onBindViewHolder(holder: StopDetailsViewHolder, position: Int) {
        holder.textView.text = myDataset[position]
    }

    override fun getItemCount(): Int = myDataset.size
}