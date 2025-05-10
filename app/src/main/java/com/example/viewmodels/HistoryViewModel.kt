package com.example.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hotelapp.classes.OrderItem
import com.example.hotelapp.repository.BookingRepository

class HistoryViewModel(private val bookingRepository: BookingRepository) : ViewModel() {

    private val _bookings = MutableLiveData<List<OrderItem>>()
    val bookings: LiveData<List<OrderItem>> get() = _bookings

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadBookings(sortBy: String, order: String) {
        bookingRepository.getMyBookingsSorted(
            sortBy = sortBy,
            order = order,
            onResult = { responseList ->
                val orderItems = responseList.map {
                    OrderItem(
                        bookingId = it.booking_id,
                        hotelName = it.hotel_name,
                        roomType = it.room_type,
                        checkInDate = it.date_start.substring(0, 10),
                        checkOutDate = it.date_end.substring(0, 10),
                        totalPrice = it.total_price,
                        status = mapStatus(it.status),
                        hotel_image_url = it.hotel_images?.firstOrNull()?.image_url ?: "",
                        room_id = it.room_id,
                        createdAt = it.created_at.substring(0, 10)
                    )
                }
                _bookings.postValue(orderItems)
            },
            onError = { _error.postValue(it.message) }
        )
    }

    private fun mapStatus(status: String?): String {
        return when (status?.lowercase()) {
            "confirmed" -> "Confirmed"
            "pending_payment", "awaiting_confirmation" -> "Pending"
            "cancelled" -> "Cancelled"
            "completed" -> "Completed"
            else -> "Unknown"
        }
    }
    fun deleteBooking(
        bookingId: Int,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        bookingRepository.deleteBooking(bookingId, onSuccess, onError)
    }

    fun archiveBooking(
        bookingId: Int,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        bookingRepository.archiveBooking(bookingId, onSuccess, onError)
    }

}