package com.example.hotelapp.ui

import HotelItem
import HotelRepository
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
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
import com.example.hotelapp.classes.Adapters.HotelImagesAdapter
import com.example.hotelapp.classes.Adapters.AmenitiesAdapter
import com.example.hotelapp.classes.Adapters.AmenityDisplay
import com.example.hotelapp.classes.AmenityMapper
import com.example.hotelapp.classes.SnackBarUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.ln

class CurrentHotelInfo : AppCompatActivity() {
    private lateinit var shimmerLayout: com.facebook.shimmer.ShimmerFrameLayout
    private lateinit var hotelRepository: HotelRepository
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: HotelImagesAdapter
    private lateinit var bottomSheet: View
    private var isFavorite: Boolean = false
    private lateinit var favoriteButton: ImageView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var rootView: View
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UserHolder.initialize(applicationContext)
        hotelRepository = UserHolder.getHotelRepository()

        setContentView(R.layout.activity_current_hotel_info)
        rootView = findViewById(R.id.main)
        loadingOverlay = findViewById(R.id.loading_overlay)
        loadingOverlay.visibility = View.VISIBLE

        findViewById<View>(R.id.main).visibility = View.GONE
        findViewById<View>(R.id.hotelDetailsBottomSheet).visibility = View.GONE

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val hotelIdFromLink = intent?.data?.getQueryParameter("id")?.toIntOrNull()

        if (hotelIdFromLink != null) {
            if (!UserHolder.getSessionManager().isLoggedIn()) {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                prefs.edit().putString("pending_deeplink", intent?.data.toString()).apply()
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
                finish()
                return
            }

            hotelRepository.getHotelById(
                hotelIdFromLink,
                onResult = { hotel ->
                    HotelHolder.currentHotel = hotel
                    initUI(hotel)
                },
                onError = {
                    SnackBarUtils.showShort(this, rootView, R.string.hotel_not_found)
                    finish()
                }
            )
            return
        }


        val hotelId = HotelHolder.currentHotel?.id ?: return finish()

        hotelRepository.getHotelById(
            hotelId,
            onResult = { hotel ->
                val favorites = UserHolder.getSessionManager().getFavoriteHotelIds()
                hotel.is_favorite = favorites.contains(hotel.id)

                HotelHolder.currentHotel = hotel
                initUI(hotel)
            },
            onError = { error ->
                SnackBarUtils.showLong(this, rootView, R.string.toast_error_with_reason, error.message ?: "")

                finish()
            }
        )
    }
    fun updateFavoriteIcon() {
        if (!::favoriteButton.isInitialized) return
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
                HotelHolder.currentHotel?.is_favorite = true
                updateFavoriteIcon()

                SnackBarUtils.showShort(this, rootView, R.string.added_to_favorites)
            },
            onError = { error ->
                SnackBarUtils.showLong(this, rootView, R.string.toast_error_with_reason, error.message ?: "")
            }

        )
    }

    override fun onResume() {
        super.onResume()
        updateFavoriteStatus()
    }



    private fun updateFavoriteStatus() {
        val hotel = HotelHolder.currentHotel
        val session = UserHolder.getSessionManager()
        val favoriteIds = session.getFavoriteHotelIds()

        Log.d("FAV_DEBUG", "Hotel ID: ${hotel?.id}")
        Log.d("FAV_DEBUG", "Favorite IDs: $favoriteIds")

        isFavorite = hotel?.id?.let { favoriteIds.contains(it) } ?: false
        updateFavoriteIcon()
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

                HotelHolder.currentHotel?.is_favorite = false
                updateFavoriteIcon()

                SnackBarUtils.showShort(this, rootView, R.string.removed_from_favorites)
            },
            onError = { error ->
                SnackBarUtils.showLong(this, rootView, R.string.toast_error_with_reason, error.message ?: "")
            }

        )
    }

    fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
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
        updateFavoriteIcon()


        val shareButton: ImageView = findViewById(R.id.share_button)
        Log.d("SHARE_DEBUG", "Share button: $shareButton")
        shareButton.setOnClickListener {

            val prettyAddress = listOfNotNull(
                hotel.address.street,
                hotel.address.city,
                hotel.address.state,
                hotel.address.country,
                hotel.address.postal_code
            ).joinToString(", ")

            val webShareLink = "https://xxxdorixxx.github.io/hotelapp-links/hotel.html?id=${hotel.id}"

            val mapsLink = "https://www.google.com/maps?q=${hotel.address.latitude},${hotel.address.longitude}"

            val shareText = """
        🏨 ${hotel.name}
        📍 $prettyAddress
        ⭐ ${getString(R.string.rating)}: ${hotel.rating}
        👁️ ${hotel.views} views

        📌 ${getString(R.string.view_on_map)}:
        $mapsLink

        🔗 ${getString(R.string.view_or_book)}:
        $webShareLink
    """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                Log.d("SHARE_DEBUG", "Share intent: $shareText")
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser_title)))
        }



        val amenities = hotel.amenities.map {
            AmenityMapper.mapAmenity(it.amenity_id)
        }

        val amenitiesAdapter = AmenitiesAdapter(amenities)
        amenitiesView.adapter = amenitiesAdapter
        amenitiesView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteIcon()

            if (isFavorite) {
                addToFavorites(hotel)
                SnackBarUtils.showLong(this, rootView, R.string.added_to_favorites)
            } else {
                removeFromFavorites(hotel.id)
                SnackBarUtils.showShort(this, rootView, R.string.removed_from_favorites)
            }

        }
        hotelName.text = hotel.name
        descriptionText.text = hotel.description
        ratingBar.rating = hotel.rating
        viewsText.text = getString(R.string.reviews_count, hotel.views)


        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            hotelRepository.rateHotel(
                hotel.id,
                rating,
                onResult = {
                    SnackBarUtils.showShort(this, rootView, R.string.rating_submitted)
                },
                onError = { error ->
                    SnackBarUtils.showLong(this, rootView, R.string.rating_failed)
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
            peekHeight = 400
        }

        val screenHeight = getScreenHeight()
        val minHeight = (screenHeight * 0.40f).toInt()
        val maxHeight = (screenHeight * 0.50f).toInt()
        val baseHeight = (screenHeight * 0.2f).toInt()

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val adjustedOffset = 1 - slideOffset
                val scale = ln(1 + adjustedOffset * 0.5) / ln(3.0)
                val newHeight = (minHeight + baseHeight + maxHeight * scale).toInt()

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
