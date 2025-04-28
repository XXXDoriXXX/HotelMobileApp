package com.example.hotelapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.R
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.repository.BookingRepository
import com.example.hotelapp.utils.SessionManager

class BookingDetailsActivity : AppCompatActivity() {

    private var bookingId: Int = -1
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: BookingRepository
    private lateinit var hotelName: String
    private lateinit var roomType: String
    private var totalPrice: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_details)

        sessionManager = SessionManager(this)
        repository = BookingRepository(RetrofitClient.retrofit.create(HotelService::class.java), sessionManager)

        bookingId = intent.getIntExtra("bookingId", -1)
        hotelName = intent.getStringExtra("hotelName") ?: ""
        roomType = intent.getStringExtra("roomType") ?: ""
        val dates = intent.getStringExtra("dates")
        totalPrice = intent.getFloatExtra("totalPrice", 0f)

        findViewById<TextView>(R.id.booking_hotel_name).text = hotelName
        findViewById<TextView>(R.id.booking_room_type).text = "Тип кімнати: $roomType"
        findViewById<TextView>(R.id.booking_dates).text = "Дати: $dates"
        findViewById<TextView>(R.id.booking_price).text = "Вартість: $${totalPrice}"

        findViewById<Button>(R.id.cancel_booking_button).setOnClickListener {
            askRefundConfirmation()
        }

        findViewById<Button>(R.id.repeat_booking_button).setOnClickListener {
            Toast.makeText(this, "Функція повторного бронювання ще не реалізована ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askRefundConfirmation() {
        repository.requestRefund(
            bookingId,
            onSuccess = { refundAmount ->
                AlertDialog.Builder(this)
                    .setTitle("Скасування бронювання")
                    .setMessage("Ви отримаєте повернення $${"%.2f".format(refundAmount)}.\nВи точно хочете скасувати бронювання?")
                    .setPositiveButton("Так") { _, _ ->
                        cancelBooking()
                    }
                    .setNegativeButton("Ні", null)
                    .show()
            },
            onError = {
                Toast.makeText(this, "Не вдалося отримати інформацію про повернення ", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun cancelBooking() {
        val booking = HotelHolder.orders.find { it.hotelName == hotelName && it.roomType == roomType }
        if (booking != null) {
            booking.status = "Скасовано"
            Toast.makeText(this, "Бронювання успішно скасовано ", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Бронювання не знайдено ", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
