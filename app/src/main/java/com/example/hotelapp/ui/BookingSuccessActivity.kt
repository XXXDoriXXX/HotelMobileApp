package com.example.hotelapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.R
import com.example.hotelapp.utils.SessionManager

class BookingSuccessActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_success)

        sessionManager = SessionManager(this)

        val bookingId = intent?.data?.getQueryParameter("booking_id") ?: intent?.getStringExtra("bookingId")
        val totalPrice = intent?.getFloatExtra("totalPrice", 0f) ?: 0f
        val bookingDate = intent?.getStringExtra("bookingDate") ?: "Невідомо"

        val successAnimation = findViewById<LottieAnimationView>(R.id.successAnimation)
        val bookingText = findViewById<TextView>(R.id.bookingSuccessText)
        val bookingDateText = findViewById<TextView>(R.id.bookingDateText)
        val bookingAmountText = findViewById<TextView>(R.id.bookingAmountText)
        val viewBookingsButton = findViewById<Button>(R.id.viewBookingsButton)
        val okButton = findViewById<Button>(R.id.okButton)
        val submitRatingButton = findViewById<Button>(R.id.submitRatingButton)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)

        val userName = sessionManager.getUserFirstName() ?: "Користувач"

        bookingText.text = if (bookingId != null) {
            "Бронювання №$bookingId підтверджено 🎉"
        } else {
            "Бронювання підтверджено 🎉"
        }
        bookingDateText.text = "Дата бронювання: $bookingDate"
        bookingAmountText.text = "Сума: $${"%.2f".format(totalPrice)}"

        successAnimation.setAnimation(R.raw.success_animation)
        successAnimation.playAnimation()

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

        submitRatingButton.setOnClickListener {
            val rating = ratingBar.rating
            if (rating == 0f) {
                Toast.makeText(this, "Оцініть готель перед надсиланням", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentHotel = HotelHolder.currentHotel
            val hotelRepo = UserHolder.getHotelRepository()

            if (currentHotel != null) {
                hotelRepo.rateHotel(
                    hotelId = currentHotel.id,
                    rating = rating,
                    onResult = {
                        Toast.makeText(this, "Рейтинг надіслано!", Toast.LENGTH_SHORT).show()
                        submitRatingButton.isEnabled = false
                        ratingBar.isEnabled = false
                    },
                    onError = {
                        Toast.makeText(this, "Помилка при надсиланні рейтингу", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, "Готель не знайдено", Toast.LENGTH_SHORT).show()
            }
        }
    }
}