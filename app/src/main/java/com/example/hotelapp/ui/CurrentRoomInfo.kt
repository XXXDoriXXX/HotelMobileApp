package com.example.hotelapp.ui

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.R
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.classes.Adapters.AmenitiesAdapter
import com.example.hotelapp.classes.BusyDatesValidator
import com.example.hotelapp.classes.CompositeValidator
import com.example.hotelapp.classes.Adapters.RoomImagesAdapter
import com.example.hotelapp.classes.AmenityMapper
import com.example.hotelapp.classes.SnackBarUtils
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.repository.BookingRepository
import com.example.hotelapp.repository.RoomRepository
import com.example.hotelapp.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ln

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
    private lateinit var loadingDialog: AlertDialog
    private lateinit var room_price:TextView
    private lateinit var rootView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_room_info)
        roomRepository = RoomRepository()
        rootView = findViewById(R.id.main)
        sessionManager = SessionManager(this)
        room_price = findViewById(R.id.room_price)
        room_price.text = getString(R.string.room_price_format, pricePerNight.toString())
        selectDatesButton = findViewById(R.id.select_dates_button)
        totalPriceText = findViewById(R.id.total_price)
        payButton = findViewById(R.id.pay_button)
        paymentSpinner = findViewById(R.id.payment_method_spinner)
        val paymentMethods = if (HotelHolder.currentHotel?.is_card_available == true) {
            listOf(getString(R.string.payment_card), getString(R.string.payment_cash))
        } else {
            listOf(getString(R.string.payment_cash))
        }



        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentMethods)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paymentSpinner.adapter = spinnerAdapter

        selectDatesButton.setOnClickListener { loadBookedDatesAndShowPicker() }
        val amenities = HotelHolder.currentRoom?.amenities?.map {
            AmenityMapper.mapAmenity(it.amenity_id)
        } ?: listOf()

        val amenitiesAdapter = AmenitiesAdapter(amenities)
        findViewById<RecyclerView>(R.id.roomAmenitiesRecyclerView).apply {
            adapter = amenitiesAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        }

        payButton.setOnClickListener {
            Log.d("CHECKOUT_DEBUG", "TotalNight: ${totalNights}")
            if (totalNights > 0) {
                val selectedMethod = paymentSpinner.selectedItem.toString()
                when (selectedMethod) {
                    "Card","Карта" -> startCheckoutFlow()
                    "Cash","Готівка" -> startCashBooking()
                }
            } else {
                SnackBarUtils.showShort(this, rootView, R.string.error_select_dates)
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
            peekHeight = (getScreenHeight() * 0.3f).toInt()
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
        val totalPrice = 0.0
        val formattedTotal = String.format("%.2f", totalPrice)
        totalPriceText.text = getString(R.string.total_price_format, formattedTotal)

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
            .setTitleText(getString(R.string.dialog_title_select_dates))
            .setCalendarConstraints(constraintsBuilder.build())
            .build()

        dateRangePicker.show(supportFragmentManager, "date_range_picker")

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second

            if (startDate != null && endDate != null) {
                val hasOverlap = busyDates.any { (busyStart, busyEnd) ->
                    startDate <= busyEnd && endDate >= busyStart
                }

                if (hasOverlap) {
                    SnackBarUtils.showShort(this, rootView, R.string.error_already_booked)

                    return@addOnPositiveButtonClickListener
                }

                val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                checkInDateFormatted = dateFormatter.format(Date(startDate))
                checkOutDateFormatted = dateFormatter.format(Date(endDate))

                selectDatesButton.text = "$checkInDateFormatted - $checkOutDateFormatted"
                calculateTotalPrice(startDate, endDate)
            }
        }

    }

    private fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    private fun calculateTotalPrice(startDateMillis: Long, endDateMillis: Long) {
        val diffInMillis = endDateMillis - startDateMillis
        totalNights = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
        val totalPrice = totalNights * pricePerNight
        val formattedTotal = String.format("%.2f", totalPrice)
        totalPriceText.text = getString(R.string.total_price_format, formattedTotal)

    }

    private fun loadBookedDatesAndShowPicker() {
        val roomId = HotelHolder.currentRoom?.id ?: return

        roomRepository.getBookedDates(roomId) { bookedDates, error ->
            if (error != null) {
                SnackBarUtils.showShort(this, rootView, R.string.hotel_not_found)
                SnackBarUtils.showShort(this, rootView, R.string.booking_load_error)
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
        Log.d("CHECKOUT_DEBUG", "Card payment")
        val roomId = HotelHolder.currentRoom?.id ?: return
        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStart = checkInDateFormatted?.let { outputFormat.format(inputFormat.parse(it)!!) } ?: return
        val dateEnd = checkOutDateFormatted?.let { outputFormat.format(inputFormat.parse(it)!!) } ?: return

        val selectedPaymentMethod = paymentSpinner.selectedItem.toString().lowercase()
        if (selectedPaymentMethod == "card" && HotelHolder.currentHotel?.is_card_available == false) {
            SnackBarUtils.showShort(this, rootView, R.string.card_not_available)
            return
        }

        showLoadingDialog(this)


        val apiService = RetrofitClient.retrofit.create(HotelService::class.java)
        val repository = BookingRepository(apiService, sessionManager)

        repository.createCheckout(roomId, dateStart, dateEnd, selectedPaymentMethod, { url ->
            hideLoadingDialog()

            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(this, Uri.parse(url))
        }, { error ->
            hideLoadingDialog()
            SnackBarUtils.showShort(this, rootView, R.string.booking_error_with_reason, error.message ?: "")
        })
    }
    fun showLoadingDialog(context: Context) {

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        loadingDialog = MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        loadingDialog.show()
    }

    fun hideLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }
    private fun startCashBooking() {
        Log.d("CHECKOUT_DEBUG", "Cash payment")
        val roomId = HotelHolder.currentRoom?.id ?: return
        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStart = checkInDateFormatted?.let { outputFormat.format(inputFormat.parse(it)!!) } ?: return
        val dateEnd = checkOutDateFormatted?.let { outputFormat.format(inputFormat.parse(it)!!) } ?: return

        showLoadingDialog(this)


        val apiService = RetrofitClient.retrofit.create(HotelService::class.java)
        val repository = BookingRepository(apiService, sessionManager)

        repository.createCheckout(roomId, dateStart, dateEnd, "cash", { urlOrMessage ->
            hideLoadingDialog()


            val intent = Intent(this, BookingSuccessActivity::class.java).apply {
                putExtra("totalPrice", totalNights * pricePerNight)
                putExtra("bookingDate", dateStart)
            }
            startActivity(intent)
            finish()

        }, { error ->
            hideLoadingDialog()
            SnackBarUtils.showShort(this, rootView, R.string.booking_error_with_reason, error.message ?: "")
        })
    }


}
