package com.example.hotelapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hotelapp.repository.BookingRepository

class BookingViewModel(private val repo: BookingRepository) : ViewModel() {
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun startBooking(
        roomId: Int,
        dateStart: String,
        dateEnd: String,
        paymentMethod: String,
        onRedirect: (String) -> Unit
    ) {
        isLoading.value = true
        repo.createCheckout(roomId, dateStart, dateEnd, paymentMethod, {
            isLoading.value = false
            onRedirect(it)
        }, {
            isLoading.value = false
            error.value = it.message
        })
    }

}
