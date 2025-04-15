package com.example.hotelapp

import HotelRepository
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.Holder.HotelHolder.currentHotel
import com.example.hotelapp.Holder.apiHolder
import com.example.hotelapp.adapters.HotelImagesAdapter
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.classes.ItemsHotelAdapter
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CurrentHotelInfo : AppCompatActivity() {
    val hotelRepository = UserHolder.getHotelRepository()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: HotelImagesAdapter
    private lateinit var bottomSheet: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_current_hotel_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val hotelname:TextView = findViewById(R.id.hotel_name)
        bottomSheet = findViewById(R.id.hotelDetailsBottomSheet)
        val ratingBar: RatingBar = findViewById(R.id.rating_bar)
        val button_book:Button = findViewById(R.id.book_now_button)
        val review:TextView = findViewById(R.id.review_count)
        val description:TextView=findViewById(R.id.hotel_description)
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            currentHotel?.let { hotel ->
                sendRatingToServer(hotel.id, rating)
            }
        }
        description.text = currentHotel!!.description
        review.text = currentHotel?.toString()+" reviews"

        hotelRepository.incrementViews(
            currentHotel!!.id,
            onResult = { response ->

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
        viewPager = findViewById(R.id.hotelImagesViewPager)

        val images = HotelHolder.currentHotel?.images?.map { it.image_url } ?: listOf()
        adapter = HotelImagesAdapter(images)
        viewPager.adapter = adapter

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = false
            peekHeight = 600
        }
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val minHeight = 550
                val maxHeight = 750
                val adjustedOffset = 1 - slideOffset

                val newHeight = (minHeight+1050 + (adjustedOffset * (maxHeight))).toInt()
                viewPager.layoutParams.height = newHeight
                viewPager.requestLayout()
            }
        })
        viewPager.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
            false
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
