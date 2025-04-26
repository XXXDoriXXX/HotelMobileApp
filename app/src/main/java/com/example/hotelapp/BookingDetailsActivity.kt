package com.example.hotelapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.classes.ItemHistoryAdapter

class BookingDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_details)

        val hotelName = intent.getStringExtra("hotelName")
        val roomType = intent.getStringExtra("roomType")
        val dates = intent.getStringExtra("dates")
        val totalPrice = intent.getFloatExtra("totalPrice", 0f)

        findViewById<TextView>(R.id.booking_hotel_name).text = hotelName
        findViewById<TextView>(R.id.booking_room_type).text = "Тип кімнати: $roomType"
        findViewById<TextView>(R.id.booking_dates).text = "Дати: $dates"
        findViewById<TextView>(R.id.booking_price).text = "Вартість: $${totalPrice}"

        findViewById<Button>(R.id.cancel_booking_button).setOnClickListener {

            val booking = HotelHolder.orders.find { it.hotelName == hotelName && it.roomType == roomType }
            if (booking != null) {
                booking.status = "Скасовано"
                Toast.makeText(this, "Бронювання скасовано", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Бронювання не знайдено", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }
}
