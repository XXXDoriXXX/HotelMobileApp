package com.example.hotelapp.repository

import com.example.hotelapp.api.HotelService
import com.example.hotelapp.models.BookingRequest
import com.example.hotelapp.models.BookingResponse
import com.example.hotelapp.models.StripePaymentResponse
import com.example.hotelapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookingRepository(private val api: HotelService, private val session: SessionManager) {

    fun getMyBookings(onResult: (List<BookingResponse>) -> Unit, onError: (Throwable) -> Unit) {
        val token = session.getAccessToken() ?: return onError(Throwable("No token"))

        api.getMyBookings("Bearer $token").enqueue(object : Callback<List<BookingResponse>> {
            override fun onResponse(
                call: Call<List<BookingResponse>>,
                response: Response<List<BookingResponse>>
            ) {
                if (response.isSuccessful) {
                    onResult(response.body() ?: emptyList())
                } else {
                    onError(Throwable("Failed to fetch booking history"))
                }
            }

            override fun onFailure(call: Call<List<BookingResponse>>, t: Throwable) {
                onError(t)
            }
        })
    }

    fun createCheckout(
        roomId: Int,
        dateStart: String,
        dateEnd: String,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val token = session.getAccessToken() ?: return onFailure(Throwable("No token"))
        val request = BookingRequest(room_id = roomId, date_start = dateStart, date_end = dateEnd)

        api.createBookingCheckout(request, "Bearer $token")
            .enqueue(object : Callback<StripePaymentResponse> {
                override fun onResponse(call: Call<StripePaymentResponse>, response: Response<StripePaymentResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.checkoutUrl?.let(onSuccess)
                    } else {
                        onFailure(Throwable("Не вдалося отримати checkout URL"))
                    }
                }

                override fun onFailure(call: Call<StripePaymentResponse>, t: Throwable) {
                    onFailure(t)
                }
            })
    }
}
