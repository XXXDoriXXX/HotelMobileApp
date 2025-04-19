package com.example.hotelapp.classes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.R

class FiltersAdapter(
    private val filters: MutableList<Pair<String, String>>,
    private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<FiltersAdapter.FilterViewHolder>() {

    inner class FilterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.chip_label)
        val close: ImageView = view.findViewById(R.id.chip_close)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filter_chip, parent, false)
        return FilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val (key, value) = filters[position]
        holder.label.text = "$key: $value"

        holder.close.setOnClickListener {
            filters.removeAt(position)
            notifyItemRemoved(position)
            onRemove(key.lowercase())
        }
    }

    override fun getItemCount(): Int = filters.size
}
