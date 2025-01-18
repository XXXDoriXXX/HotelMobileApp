package com.example.hotelapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.Holder.apiHolder
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

        val roomimage: ImageView = findViewById(R.id.room_image)
        val backbtn:ImageView = findViewById(R.id.back_button)
        backbtn.setOnClickListener {
            finish()
        }
        val imageUrl = HotelHolder.currentRoom?.images?.firstOrNull()?.image_url
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(apiHolder.BASE_URL+imageUrl)
                .placeholder(R.drawable.default_image)
                .into(roomimage)
        } else {
            roomimage.setImageResource(R.drawable.default_image)
        }
        val roomnum:TextView = findViewById(R.id.room_number)
        val roomPrice:TextView=findViewById(R.id.room_price)
        roomnum.text ="Room number: "+ HotelHolder.currentRoom?.room_number.toString()
        roomPrice.text ="$"+ HotelHolder.currentRoom?.price_per_night.toString()
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