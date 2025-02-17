package com.example.hotelapp

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.Holder.apiHolder
import com.example.hotelapp.R.id.roomDetailsBottomSheet
import com.example.hotelapp.adapters.HotelImagesAdapter
import com.example.hotelapp.classes.RoomImagesAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class Current_Room_Info : AppCompatActivity() {
    private lateinit var selectDatesButton: Button
    private lateinit var totalPriceText: TextView
    private var pricePerNight = HotelHolder.currentRoom?.price_per_night?: 0f
    private var totalNights = 0
    private  var checkInDateFormatted: String? = null
    private  var checkOutDateFormatted: String? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: RoomImagesAdapter
    private lateinit var bottomSheet: View
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
                val newOrder = checkInDateFormatted?.let { checkIn ->
                    checkOutDateFormatted?.let { checkOut ->
                        OrderItem(
                            hotelName = HotelHolder.currentHotel?.name ?: "Unknown Hotel",
                            roomType = HotelHolder.currentRoom?.room_type ?: "Unknown Room",
                            checkInDate = checkIn,
                            checkOutDate = checkOut,
                            totalPrice = totalNights * pricePerNight
                        )
                    }
                }

                if (newOrder != null) {
                    HotelHolder.orders.add(newOrder)
                    Toast.makeText(
                        this,
                        "Payment of \$${totalNights * pricePerNight} confirmed!",
                        Toast.LENGTH_SHORT
                    ).show()

                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigation_fragment) as? NavHostFragment
                    val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment
                    if (currentFragment is HistoryFragment) {
                        currentFragment.addOrderToList(newOrder)
                    }


                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to create order.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please select dates first.", Toast.LENGTH_SHORT).show()
            }
        }

        val descriprion:TextView=findViewById(R.id.room_description)
        descriprion.text=HotelHolder.currentRoom!!.description
        val backbtn:ImageView = findViewById(R.id.back_button)
        backbtn.setOnClickListener {
            finish()
        }
        val roomnum:TextView = findViewById(R.id.room_number)
        val roomPrice:TextView=findViewById(R.id.room_price)
        roomnum.text ="Room number: "+ HotelHolder.currentRoom?.room_number.toString()
        roomPrice.text ="$"+ HotelHolder.currentRoom?.price_per_night.toString()

        bottomSheet = findViewById(roomDetailsBottomSheet)
        viewPager = findViewById(R.id.roomImagesViewPager)

        val images = HotelHolder.currentRoom?.images?.map { it.image_url } ?: listOf()
        adapter = RoomImagesAdapter(images)
        viewPager.adapter = adapter

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = false
            peekHeight = 700
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
                checkInDateFormatted = startDateFormatted
                checkOutDateFormatted = endDateFormatted
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