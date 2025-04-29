package com.example.hotelapp.ui

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.viewpager2.widget.ViewPager2
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.R
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.classes.BusyDatesValidator
import com.example.hotelapp.classes.CompositeValidator
import com.example.hotelapp.classes.RoomImagesAdapter
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.repository.BookingRepository
import com.example.hotelapp.repository.RoomRepository
import com.example.hotelapp.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CurrentRoomInfo : AppCompatActivity() {

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
    private lateinit var paymentSpinner: Spinner
    private lateinit var sessionManager: SessionManager
    private var busyDates: List<Pair<Long, Long>> = listOf()
    private lateinit var roomRepository: RoomRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_room_info)
        roomRepository = RoomRepository()

        sessionManager = SessionManager(this)

        selectDatesButton = findViewById(R.id.select_dates_button)
        totalPriceText = findViewById(R.id.total_price)
        payButton = findViewById(R.id.pay_button)
        paymentSpinner = findViewById(R.id.payment_method_spinner)

        val paymentMethods = listOf("Card", "Cash", "Google Pay")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentMethods)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paymentSpinner.adapter = spinnerAdapter

        selectDatesButton.setOnClickListener { loadBookedDatesAndShowPicker() }


        payButton.setOnClickListener {
            if (totalNights > 0) {
                val selectedMethod = paymentSpinner.selectedItem.toString()
                when (selectedMethod) {
                    "Card" -> startCheckoutFlow()
                    "Cash" -> startCashBooking()
                    "Google Pay" -> Toast.makeText(this, "Google Pay ще не реалізовано", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Виберіть дати проживання", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheet = findViewById(R.id.roomDetailsBottomSheet)
        viewPager = findViewById(R.id.roomImagesViewPager)
        val images = HotelHolder.currentRoom?.images?.map { it.image_url } ?: listOf()
        adapter = RoomImagesAdapter(images)
        viewPager.adapter = adapter

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = false
            peekHeight = 700
        }

        viewPager.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            false
        }
    }

    private fun showDatePicker() {
        val validator = CompositeValidator(
            listOf(
                BusyDatesValidator(busyDates),
                DateValidatorPointForward.from(System.currentTimeMillis() - 3600000)

            )
        )

        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(validator)

        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Виберіть дати")
            .setCalendarConstraints(constraintsBuilder.build())
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

    private fun loadBookedDatesAndShowPicker() {
        val roomId = HotelHolder.currentRoom?.id ?: return

        roomRepository.getBookedDates(roomId) { bookedDates, error ->
            if (error != null) {
                Toast.makeText(this, "Не вдалося завантажити заброньовані дати", Toast.LENGTH_SHORT).show()
                return@getBookedDates
            }

            busyDates = bookedDates?.map {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                Pair(
                    sdf.parse(it.start_date)!!.time,
                    sdf.parse(it.end_date)!!.time
                )
            } ?: listOf()

            showDatePicker()
        }
    }

    private fun startCheckoutFlow() {
        val roomId = HotelHolder.currentRoom?.id ?: return
        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStart = checkInDateFormatted?.let { outputFormat.format(inputFormat.parse(it)!!) } ?: return
        val dateEnd = checkOutDateFormatted?.let { outputFormat.format(inputFormat.parse(it)!!) } ?: return

        val selectedPaymentMethod = paymentSpinner.selectedItem.toString().lowercase()

        val progress = ProgressDialog(this).apply {
            setMessage("Створюємо бронювання...")
            setCancelable(false)
            show()
        }

        val apiService = RetrofitClient.retrofit.create(HotelService::class.java)
        val repository = BookingRepository(apiService, sessionManager)

        repository.createCheckout(roomId, dateStart, dateEnd, selectedPaymentMethod, { url ->
            progress.dismiss()
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(this, Uri.parse(url))
        }, { error ->
            progress.dismiss()
            Toast.makeText(this, "Помилка: ${error.message}", Toast.LENGTH_LONG).show()
        })
    }
    private fun startCashBooking() {
        val roomId = HotelHolder.currentRoom?.id ?: return
        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStart = checkInDateFormatted?.let { outputFormat.format(inputFormat.parse(it)!!) } ?: return
        val dateEnd = checkOutDateFormatted?.let { outputFormat.format(inputFormat.parse(it)!!) } ?: return

        val progress = ProgressDialog(this).apply {
            setMessage("Створюємо бронювання...")
            setCancelable(false)
            show()
        }

        val apiService = RetrofitClient.retrofit.create(HotelService::class.java)
        val repository = BookingRepository(apiService, sessionManager)

        repository.createCheckout(roomId, dateStart, dateEnd, "cash", { urlOrMessage ->
            progress.dismiss()

            val intent = Intent(this, BookingSuccessActivity::class.java).apply {
                putExtra("totalPrice", totalNights * pricePerNight)
                putExtra("bookingDate", dateStart)
            }
            startActivity(intent)
            finish()

        }, { error ->
            progress.dismiss()
            Toast.makeText(this, "Помилка: ${error.message}", Toast.LENGTH_LONG).show()
        })
    }


}
