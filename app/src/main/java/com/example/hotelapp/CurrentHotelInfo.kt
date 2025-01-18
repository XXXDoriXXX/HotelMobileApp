package com.example.hotelapp

import HotelRepository
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.Holder.HotelHolder.currentHotel
import com.example.hotelapp.Holder.UserHolder
import com.example.hotelapp.Holder.apiHolder
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.utils.SessionManager

class CurrentHotelInfo : AppCompatActivity() {
    val hotelRepository = UserHolder.getHotelRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_current_hotel_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val hotelimage:ImageView = findViewById(R.id.hotel_image)
        val hotelname:TextView = findViewById(R.id.hotel_name)
        val hoteldesc:TextView=findViewById(R.id.hotel_description)
        val ratingBar: RatingBar = findViewById(R.id.rating_bar)
        val button_book:Button = findViewById(R.id.book_now_button)
        val review:TextView = findViewById(R.id.review_count)
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            currentHotel?.let { hotel ->
                sendRatingToServer(hotel.id, rating)
            }
        }
        review.text = currentHotel?.views.toString()+" reviews"
        val imageUrl = HotelHolder.currentHotel?.images?.firstOrNull()?.image_url
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(apiHolder.BASE_URL+imageUrl)
                .placeholder(R.drawable.default_image)
                .into(hotelimage)
        } else {
            hotelimage.setImageResource(R.drawable.default_image)
        }

        hotelRepository.incrementViews(
            currentHotel!!.id,
            onResult = { response ->
                currentHotel!!.views=response.views
                review.text=response.views.toString()+" reviews"
                println("Views updated: ${response.views}")
            },
            onError = { error ->
                println("Failed to update views: ${error.message}")
            }
        )
        hotelname.text = HotelHolder.currentHotel?.name
        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
        button_book.setOnClickListener {
            startActivity(Intent(this, RoomsListActivity::class.java))
        }

    }
    private fun sendRatingToServer(hotelId: Int, rating: Float) {
        hotelRepository.rateHotel(hotelId, rating,
            onResult = { response ->
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                Toast.makeText(this, "Failed to submit rating: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }



}
