package com.example.hotelapp

import UserHolder
import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.classes.RoomImagesAdapter
import com.example.hotelapp.models.PaymentRequest

import com.example.hotelapp.R.id.roomDetailsBottomSheet
import com.example.hotelapp.models.PaymentSuccessResponse
import com.example.hotelapp.models.PaymentSuccessRequest
import com.example.hotelapp.models.StripePaymentResponse
import com.example.hotelapp.network.RetrofitClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.datepicker.MaterialDatePicker
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class Current_Room_Info : AppCompatActivity() {

    private lateinit var selectDatesButton: Button
    private lateinit var totalPriceText: TextView
    private lateinit var payButton: Button
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var pricePerNight = HotelHolder.currentRoom?.price_per_night ?: 0f
    private var totalNights = 0
    private var checkInDateFormatted: String? = null
    private var checkOutDateFormatted: String? = null
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: RoomImagesAdapter
    private lateinit var bottomSheet: View
    private lateinit var paymentSheet: PaymentSheet
    private var paymentIntentClientSecret: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_room_info)

        selectDatesButton = findViewById(R.id.select_dates_button)
        totalPriceText = findViewById(R.id.total_price)
        payButton = findViewById(R.id.pay_button)


        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51QvkR6GEyS0IOVdo5SlLrdTyZZUpbbGk43hrF1S21dgLW1ezHtwJwFvYbkY6cIZaTBU6m1rtylA5URnQ020KWLcb00N6N8OD8r"
        )
        paymentSheet = PaymentSheet(this) { paymentSheetResult ->
            when (paymentSheetResult) {
                is PaymentSheetResult.Completed -> {
                    Toast.makeText(this, "Оплата успішна!", Toast.LENGTH_LONG).show()
                    notifyPaymentSuccess()
                }
                is PaymentSheetResult.Canceled -> {
                    Toast.makeText(this, "Оплата скасована", Toast.LENGTH_SHORT).show()
                }
                is PaymentSheetResult.Failed -> {
                    Toast.makeText(this, "Помилка: ${paymentSheetResult.error.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
        selectDatesButton.setOnClickListener { showDatePicker() }

        payButton.setOnClickListener {
            if (totalNights > 0) {
                fetchPaymentIntent()
            } else {
                Toast.makeText(this, "Спочатку виберіть дати", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun fetchPaymentIntent() {
        val totalPrice = totalNights * pricePerNight

        val paymentRequest = PaymentRequest(amount = totalPrice)
        val apiService = RetrofitClient.retrofit.create(HotelService::class.java)
        apiService.createPaymentIntent(paymentRequest).enqueue(object : Callback<StripePaymentResponse> {
            override fun onResponse(call: Call<StripePaymentResponse>, response: Response<StripePaymentResponse>) {
                if (response.isSuccessful) {
                    val stripeResponse = response.body()
                    paymentIntentClientSecret = stripeResponse?.clientSecret
                    presentPaymentSheet()
                } else {
                    Toast.makeText(this@Current_Room_Info, "Помилка отримання даних оплати", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<StripePaymentResponse>, t: Throwable) {
                Toast.makeText(this@Current_Room_Info, "Помилка: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun presentPaymentSheet() {
        paymentIntentClientSecret?.let { clientSecret ->
            val configuration = PaymentSheet.Configuration(
                merchantDisplayName = "HotelAPP",
                googlePay = PaymentSheet.GooglePayConfiguration(
                    environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
                    countryCode = "US",
                    currencyCode = "usd"
                )
            )
            paymentSheet.presentWithPaymentIntent(clientSecret, configuration)
        }
    }

    private fun showDatePicker() {
        val dateRangePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Виберіть дати")
            .build()

        dateRangePicker.show(supportFragmentManager, "date_range_picker")

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second

            if (startDate != null && endDate != null) {
                val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                checkInDateFormatted = dateFormatter.format(Date(startDate))
                checkOutDateFormatted = dateFormatter.format(Date(endDate))

                selectDatesButton.text = "$checkInDateFormatted - $checkOutDateFormatted"
                calculateTotalPrice(startDate, endDate)
            }
        }
    }

    private fun calculateTotalPrice(startDateMillis: Long, endDateMillis: Long) {
        val diffInMillis = endDateMillis - startDateMillis
        totalNights = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
        totalPriceText.text = "Total: $${totalNights * pricePerNight}"
    }
    private fun notifyPaymentSuccess() {
        val clientId = UserHolder.currentUser?.id?:0
        val roomId = HotelHolder.currentRoom?.id ?: 0

        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStart = checkInDateFormatted?.let { outputFormat.format(inputFormat.parse(it)!!) } ?: ""
        val dateEnd = checkOutDateFormatted?.let { outputFormat.format(inputFormat.parse(it)!!) } ?: ""

        val totalPrice = totalNights * pricePerNight
        val amount = totalPrice

        val paymentSuccessRequest = PaymentSuccessRequest(
            client_id = clientId,
            room_id = roomId,
            date_start = dateStart,
            date_end = dateEnd,
            total_price = totalPrice,
            amount = amount
        )

        val apiService = RetrofitClient.retrofit.create(HotelService::class.java)
        apiService.notifyPaymentSuccess(paymentSuccessRequest).enqueue(object : Callback<PaymentSuccessResponse> {
            override fun onResponse(call: Call<PaymentSuccessResponse>, response: Response<PaymentSuccessResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@Current_Room_Info, "Бронювання створено", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@Current_Room_Info, "Помилка при повідомленні бекенду", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<PaymentSuccessResponse>, t: Throwable) {
                Toast.makeText(this@Current_Room_Info, "Помилка: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}