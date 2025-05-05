package com.example.hotelapp.classes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.R
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FiltersAdapter(
    private val filters: MutableList<Pair<String, String>>,
    private val onRemove: (String) -> Unit,
    private val onUpdate: (String, String) -> Unit
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
        if (position >= filters.size) return

        val (key, value) = filters[position]
        holder.label.text = "$key: $value"

        holder.close.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_POSITION && currentPos < filters.size) {
                val removedKey = filters[currentPos].first
                onRemove(removedKey.lowercase())
            }

        }

        holder.view.setOnClickListener {
            val context = holder.view.context
            if (key.equals("Check_Date", ignoreCase = true)) {
                val validator = CompositeDateValidator.allOf(listOf(DateValidatorPointForward.now()))
                val constraints = CalendarConstraints.Builder().setValidator(validator).build()

                val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Update Check-in and Check-out")
                    .setCalendarConstraints(constraints)
                    .build()

                dateRangePicker.show((context as AppCompatActivity).supportFragmentManager, "edit_check_date")

                dateRangePicker.addOnPositiveButtonClickListener { selection ->
                    val startDate = selection.first
                    val endDate = selection.second

                    if (startDate != null && endDate != null) {
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val checkIn = formatter.format(Date(startDate))
                        val checkOut = formatter.format(Date(endDate))
                        val label = "$checkIn → $checkOut"

                        filters[position] = Pair("Check_Date", label)
                        notifyItemChanged(position)
                        onUpdate("check_in", checkIn)
                        onUpdate("check_out", checkOut)
                    }
                }
            } else {
                val input = EditText(context).apply {
                    setText(value)
                    hint = "Enter new value for $key"
                }

                MaterialAlertDialogBuilder(context)
                    .setTitle("$key filter")
                    .setMessage("Current value: \"$value\"")
                    .setView(input)
                    .setPositiveButton("Update") { _, _ ->
                        val newValue = input.text.toString().trim()
                        if (newValue.isNotEmpty() && newValue != value) {
                            filters[position] = Pair(key, newValue)
                            notifyItemChanged(position)
                            onUpdate(key.lowercase(), newValue)
                        }
                    }
                    .setNegativeButton("Delete") { _, _ ->
                        onRemove(key.lowercase())
                    }
                    .setNeutralButton("Cancel", null)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int = filters.size
}
