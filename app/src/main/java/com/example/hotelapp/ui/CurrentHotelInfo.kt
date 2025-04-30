package com.example.hotelapp.ui

import HotelItem
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.R
import com.example.hotelapp.adapters.HotelImagesAdapter
import com.example.hotelapp.classes.AmenitiesAdapter
import com.example.hotelapp.classes.AmenityDisplay
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CurrentHotelInfo : AppCompatActivity() {
    private lateinit var shimmerLayout: com.facebook.shimmer.ShimmerFrameLayout
    private val hotelRepository = UserHolder.getHotelRepository()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: HotelImagesAdapter
    private lateinit var bottomSheet: View
    private var isFavorite: Boolean = false
    private lateinit var favoriteButton: ImageView
    private lateinit var loadingOverlay: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_current_hotel_info)
        loadingOverlay = findViewById(R.id.loading_overlay)
        loadingOverlay.visibility = View.VISIBLE

        findViewById<View>(R.id.main).visibility = View.GONE
        findViewById<View>(R.id.hotelDetailsBottomSheet).visibility = View.GONE

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val hotelId = HotelHolder.currentHotel?.id ?: return finish()

        hotelRepository.getHotelById(
            hotelId,
            onResult = { hotel ->
                HotelHolder.currentHotel = hotel
                initUI(hotel)
            },
            onError = { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        )
    }
    fun updateFavoriteIcon() {
        val drawableId = if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        favoriteButton.setImageResource(drawableId)
    }
    private fun checkIfFavorite(hotelId: Int): Boolean {
        val favorites = UserHolder.getSessionManager().getFavoriteHotelIds()
        return favorites.contains(hotelId)
    }


    private fun addToFavorites(hotel: HotelItem) {
        val session = UserHolder.getSessionManager()
        val repo = UserHolder.getHotelRepository()

        repo.addFavorite(
            hotelId = hotel.id,
            onResult = {
                val ids = session.getFavoriteHotelIds().toMutableList()
                if (!ids.contains(hotel.id)) {
                    ids.add(hotel.id)
                    session.saveFavoriteHotelIds(ids)
                }
            },
            onError = {
                Toast.makeText(this, "Failed to add to favorites", Toast.LENGTH_SHORT).show()
            }
        )
    }


    private fun removeFromFavorites(hotelId: Int) {
        val session = UserHolder.getSessionManager()
        val repo = UserHolder.getHotelRepository()

        repo.removeFavorite(
            hotelId = hotelId,
            onResult = {
                val ids = session.getFavoriteHotelIds().toMutableList()
                if (ids.contains(hotelId)) {
                    ids.remove(hotelId)
                    session.saveFavoriteHotelIds(ids)
                }
            },
            onError = {
                Toast.makeText(this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show()
            }
        )
    }


    private fun initUI(hotel: HotelItem) {


        loadingOverlay.visibility = View.GONE

        findViewById<View>(R.id.main).visibility = View.VISIBLE
        bottomSheet = findViewById(R.id.hotelDetailsBottomSheet)
        bottomSheet.visibility = View.VISIBLE

        val hotelName: TextView = findViewById(R.id.hotel_name)
        val ratingBar: RatingBar = findViewById(R.id.rating_bar)
        val bookNowButton: Button = findViewById(R.id.book_now_button)
        val viewsText: TextView = findViewById(R.id.review_count)
        val descriptionText: TextView = findViewById(R.id.hotel_description)
        val backButton: ImageView = findViewById(R.id.back_button)

        viewPager = findViewById(R.id.hotelImagesViewPager)
        val amenitiesView = findViewById<RecyclerView>(R.id.amenitiesRecyclerView)
        favoriteButton = findViewById(R.id.favorite_button)
        val favoriteIds = UserHolder.getSessionManager().getFavoriteHotelIds()
        isFavorite = favoriteIds.contains(hotel.id)
        updateFavoriteIcon()


        val amenities = hotel.amenities.map {
            when (it.amenity_id) {
                1 -> AmenityDisplay(R.drawable.wifi, "Wi-Fi")
                2 -> AmenityDisplay(R.drawable.tv, "TV")
                3 -> AmenityDisplay(R.drawable.ac, "AC")
                4 -> AmenityDisplay(R.drawable.gym, "Gym")
                5 -> AmenityDisplay(R.drawable.parking, "Parking")
                else -> AmenityDisplay(R.drawable.ic_other, "Other")
            }
        }
        isFavorite = checkIfFavorite(hotel.id)
        updateFavoriteIcon()
        val amenitiesAdapter = AmenitiesAdapter(amenities)
        amenitiesView.adapter = amenitiesAdapter
        amenitiesView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteIcon()

            if (isFavorite) {
                addToFavorites(hotel)
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
            } else {
                removeFromFavorites(hotel.id)
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
            }
        }
        hotelName.text = hotel.name
        descriptionText.text = hotel.description
        ratingBar.rating = hotel.rating
        viewsText.text = "${hotel.views} views"

        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            hotelRepository.rateHotel(
                hotel.id,
                rating,
                onResult = {
                    Toast.makeText(this, "Rating submitted", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        bookNowButton.setOnClickListener {
            startActivity(Intent(this, RoomsListActivity::class.java))
        }

        backButton.setOnClickListener {
            finish()
        }

        val imageUrls = hotel.images.map { it.image_url }
        adapter = HotelImagesAdapter(imageUrls)
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
                val newHeight = (minHeight + 1050 + (adjustedOffset * maxHeight)).toInt()
                viewPager.layoutParams.height = newHeight
                viewPager.requestLayout()
            }
        })

        viewPager.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            false
        }
    }
}
