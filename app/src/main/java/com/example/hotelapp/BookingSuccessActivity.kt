package com.example.hotelapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BookingSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_success)

        val bookingId = intent?.data?.getQueryParameter("booking_id")
        val bookingText = findViewById<TextView>(R.id.bookingSuccessText)
        val viewBookingsButton = findViewById<Button>(R.id.viewBookingsButton)
        val okButton = findViewById<Button>(R.id.okButton)

        bookingText.text = if (bookingId != null) {
            "Бронювання №$bookingId підтверджено 🎉"
        } else {
            "Бронювання підтверджено 🎉"
        }

        okButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("startFragment", "home")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        viewBookingsButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("startFragment", "history")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

    }
}
