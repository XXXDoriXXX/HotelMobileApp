package com.example.hotelapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelapp.R
import com.example.hotelapp.utils.SessionManager
import com.example.hotelapp.Holder.HotelHolder

class BookingSuccessActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_success)

        sessionManager = SessionManager(this)

        val bookingId = intent?.data?.getQueryParameter("booking_id") ?: intent?.getStringExtra("bookingId")
        val totalPrice = intent?.getFloatExtra("totalPrice", 0f) ?: 0f
        val bookingDate = intent?.getStringExtra("bookingDate") ?: "Невідомо"

        val thankYouText = findViewById<TextView>(R.id.thankYouText)
        val bookingText = findViewById<TextView>(R.id.bookingSuccessText)
        val bookingDateText = findViewById<TextView>(R.id.bookingDateText)
        val bookingAmountText = findViewById<TextView>(R.id.bookingAmountText)
        val viewBookingsButton = findViewById<Button>(R.id.viewBookingsButton)
        val okButton = findViewById<Button>(R.id.okButton)

        val userName = sessionManager.getUserFirstName() ?: "Користувач"

        thankYouText.text = "Дякуємо, $userName!"
        bookingText.text = if (bookingId != null) {
            "Бронювання №$bookingId підтверджено 🎉"
        } else {
            "Бронювання підтверджено 🎉"
        }
        bookingDateText.text = "Дата бронювання: $bookingDate"
        bookingAmountText.text = "Сума: $${"%.2f".format(totalPrice)}"

        okButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("startFragment", "home")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        viewBookingsButton.setOnClickListener {
            val intent = Intent(this, BookingDetailsActivity::class.java)
            intent.putExtra("bookingId", bookingId?.toIntOrNull() ?: -1)
            startActivity(intent)
            finish()
        }
    }
}
