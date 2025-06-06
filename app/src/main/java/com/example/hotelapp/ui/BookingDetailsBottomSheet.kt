package com.example.hotelapp.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.R
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.repository.BookingRepository
import com.example.hotelapp.repository.RoomRepository
import com.example.hotelapp.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.log

class BookingDetailsBottomSheet : BottomSheetDialogFragment() {

    private var bookingId: Int = -1
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: BookingRepository
    private lateinit var hotelName: String
    private lateinit var roomType: String
    private var totalPrice: Float = 0f
    private var dates: String? = null
    private var status: String? = null
    private var paymentMethod: String? = null
    private var roomId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bookingId = it.getInt("bookingId", -1)
            hotelName = it.getString("hotelName") ?: ""
            roomType = it.getString("roomType") ?: ""
            dates = it.getString("dates")
            totalPrice = it.getFloat("totalPrice", 0f)
            status = it.getString("status")
            paymentMethod = it.getString("paymentMethod")
            roomId = it.getInt("roomId", -1)
        }

        sessionManager = SessionManager(requireContext())
        repository = BookingRepository(RetrofitClient.retrofit.create(HotelService::class.java), sessionManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.activity_booking_details, container, false)
        val cancelButton = view.findViewById<Button>(R.id.cancel_booking_button)
        if (status == "Cancelled") {
            cancelButton.visibility = View.GONE
        }
        val status_background = view.findViewById<FrameLayout>(R.id.status_background)
        Log.d("DEBUG", "status: $status")
        when (status) {
            "Pending" -> status_background.setBackgroundResource(R.drawable.status_label_background_yellow)
            "Confirmed" -> status_background.setBackgroundResource(R.drawable.status_label_background_green)
            "Cancelled" -> status_background.setBackgroundResource(R.drawable.status_label_background_red)
        }

        view.findViewById<TextView>(R.id.booking_hotel_name).text = hotelName
        view.findViewById<TextView>(R.id.booking_room_type).text = "Тип кімнати: $roomType"
        view.findViewById<TextView>(R.id.booking_dates).text = "Дати: $dates"
        view.findViewById<TextView>(R.id.booking_price).text = "Вартість: $${totalPrice}"

        view.findViewById<Button>(R.id.cancel_booking_button).setOnClickListener {
            askRefundConfirmation()
        }

        view.findViewById<Button>(R.id.repeat_booking_button).setOnClickListener {
            if (roomId != -1) {
                val context = requireContext()
                val roomRepository = RoomRepository()

                val progressDialog = ProgressDialog(context).apply {
                    setMessage("Завантаження кімнати...")
                    setCancelable(false)
                    show()
                }
                Log.d("DEBUG", "roomId: $roomId")

                roomRepository.getRoomById(roomId) { roomItem, error ->
                    progressDialog.dismiss()

                    if (error != null || roomItem == null) {
                        Toast.makeText(context, "Не вдалося знайти кімнату", Toast.LENGTH_SHORT).show()
                        return@getRoomById
                    }

                    HotelHolder.currentRoom = roomItem

                    val intent = Intent(context, CurrentRoomInfo::class.java)
                    startActivity(intent)
                    dismiss()
                }
            } else {
                Toast.makeText(requireContext(), "Невідомий roomId", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    private fun askRefundConfirmation() {
        if (paymentMethod == "cash") {
            Toast.makeText(requireContext(), "Оплата готівкою не підлягає поверненню", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Скасувати бронювання?")
            .setMessage("Ви точно хочете скасувати це бронювання? Може бути застосовано повернення коштів.")
            .setPositiveButton("Так") { _, _ ->
                repository.requestRefund(
                    bookingId,
                    onSuccess = { refundAmount ->
                        Toast.makeText(requireContext(), "Ви отримаєте повернення $${"%.2f".format(refundAmount)}.", Toast.LENGTH_SHORT).show()
                        dismiss()
                    },
                    onError = { error ->
                        val message = error.message ?: ""
                        if ("Cannot refund within 24 hours of check-in" in message) {
                            Toast.makeText(requireContext(), "Неможливо скасувати бронювання менш ніж за 24 години до заселення.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Не вдалося отримати інформацію про повернення", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            .setNegativeButton("Ні") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    companion object {
        fun newInstance(
            bookingId: Int,
            hotelName: String,
            roomType: String,
            dates: String,
            totalPrice: Float,
            roomId: Int,
            status: String
        ) = BookingDetailsBottomSheet().apply {
            arguments = Bundle().apply {
                putInt("bookingId", bookingId)
                putString("hotelName", hotelName)
                putString("roomType", roomType)
                putString("dates", dates)
                putFloat("totalPrice", totalPrice)
                putInt("roomId", roomId)
                putString("status",status)
            }
        }
    }
}
