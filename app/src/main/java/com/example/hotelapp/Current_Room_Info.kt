package com.example.hotelapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.time.times

class Current_Room_Info : AppCompatActivity() {
    private lateinit var selectDatesButton: Button
    private lateinit var totalPriceText: TextView
    private var pricePerNight = HotelHolder.currentRoom?.price?: 0f
    private var totalNights = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_current_room_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        selectDatesButton = findViewById(R.id.select_dates_button)
        totalPriceText = findViewById(R.id.total_price)
        selectDatesButton.setOnClickListener { showDatePicker() }

        findViewById<Button>(R.id.pay_button).setOnClickListener {
            if (totalNights > 0) {
                Toast.makeText(this, "Payment of \$${totalNights * pricePerNight} confirmed!", Toast.LENGTH_SHORT).show()
                HotelHolder.currentRoom?.let { it1 -> HotelHolder.roomList.add(it1) }
            } else {
                Toast.makeText(this, "Please select dates first.", Toast.LENGTH_SHORT).show()
            }
        }
        val roomimage: ImageView = findViewById(R.id.room_image)
        val backbtn:ImageView = findViewById(R.id.back_button)
        backbtn.setOnClickListener {
            finish()
        }
        var imageid = resources.getIdentifier(
            HotelHolder.currentRoom?.image,
            "drawable",
            packageName
        )
        if (imageid != 0) {
            roomimage.setImageResource(imageid)
        } else {
            roomimage.setImageResource(R.drawable.default_hotel_image)
        }
        val roomnum:TextView = findViewById(R.id.room_number)
        val roomPrice:TextView=findViewById(R.id.room_price)
        roomnum.text ="Room number: "+ HotelHolder.currentRoom?.number.toString()
        roomPrice.text ="$"+HotelHolder.currentRoom?.price.toString()
    }
    private fun showDatePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select dates")
            .build()

        dateRangePicker.show(supportFragmentManager, "date_range_picker")

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second

            if (startDate != null && endDate != null) {
                val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val startDateFormatted = dateFormatter.format(Date(startDate))
                val endDateFormatted = dateFormatter.format(Date(endDate))

                selectDatesButton.text = "$startDateFormatted - $endDateFormatted"

                calculateTotalPrice(startDate, endDate)
            }
        }
    }
    private fun calculateTotalPrice(startDateMillis: Long, endDateMillis: Long) {
        val diffInMillis = endDateMillis - startDateMillis
        totalNights = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
        totalPriceText.text = "Total: $${totalNights * pricePerNight}"
    }
}