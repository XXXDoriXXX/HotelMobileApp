package com.example.hotelapp.repository

import com.example.hotelapp.api.HotelService
import com.example.hotelapp.models.BookingRequest
import com.example.hotelapp.models.BookingResponse
import com.example.hotelapp.models.RefundResponse
import com.example.hotelapp.models.StripePaymentResponse
import com.example.hotelapp.utils.SessionManager
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookingRepository(private val api: HotelService, private val session: SessionManager) {

    fun getMyBookingsSorted(
        sortBy: String,
        order: String,
        onResult: (List<BookingResponse>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val token = session.getAccessToken() ?: return onError(Throwable("No token"))
        api.getMyBookingsSorted("Bearer $token", sortBy, order).enqueue(object : Callback<List<BookingResponse>> {
            override fun onResponse(
                call: Call<List<BookingResponse>>,
                response: Response<List<BookingResponse>>
            ) {
                if (response.isSuccessful) {
                    onResult(response.body() ?: emptyList())
                } else {
                    onError(Throwable("Failed to fetch sorted bookings"))
                }
            }

            override fun onFailure(call: Call<List<BookingResponse>>, t: Throwable) {
                onError(t)
            }
        })
    }

    fun requestRefund(
        bookingId: Int,
        onSuccess: (Float) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val token = session.getAccessToken() ?: return onError(Throwable("No token"))
        api.requestRefund(bookingId, "Bearer $token").enqueue(object : Callback<RefundResponse> {
            override fun onResponse(call: Call<RefundResponse>, response: Response<RefundResponse>) {
                if (response.isSuccessful) {
                    val refund = response.body()?.refunded ?: 0f
                    onSuccess(refund)
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string()
                            ?.let { com.google.gson.JsonParser.parseString(it).asJsonObject["detail"]?.asString }
                            ?: "Failed to fetch refund amount"
                    } catch (e: Exception) {
                        "Failed to fetch refund amount"
                    }
                    onError(Throwable(errorMessage))

                }
            }

            override fun onFailure(call: Call<RefundResponse>, t: Throwable) {
                onError(t)
            }
        })
    }

    fun createCheckout(
        roomId: Int,
        dateStart: String,
        dateEnd: String,
        paymentMethod: String,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val token = session.getAccessToken() ?: return onFailure(Throwable("No token"))
        val request = BookingRequest(
            room_id = roomId,
            date_start = dateStart,
            date_end = dateEnd,
            payment_method = paymentMethod
        )

        api.createBookingCheckoutRaw(request, "Bearer $token")
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            when {
                                body.has("checkout_url") -> {
                                    val checkoutUrl = body.get("checkout_url").asString
                                    onSuccess(checkoutUrl)
                                }
                                body.has("message") -> {
                                    val message = body.get("message").asString
                                    onSuccess(message)
                                }
                                else -> {
                                    onFailure(Throwable("Unknown response structure"))
                                }
                            }
                        } else {
                            onFailure(Throwable("Empty response body"))
                        }
                    } else {
                        val errorMessage = try {
                            response.errorBody()?.string()
                                ?.let { com.google.gson.JsonParser.parseString(it).asJsonObject["detail"]?.asString }
                                ?: "Booking creation failed"
                        } catch (e: Exception) {
                            "Booking creation failed"
                        }

                        onFailure(Throwable(errorMessage))
                    }

                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    onFailure(t)
                }
            })
    }
    fun deleteBooking(
        bookingId: Int,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    )
    {
        val token = session.getAccessToken() ?: return onError(Throwable("No token"))
        api.deleteBooking("Bearer $token", bookingId).enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Throwable("Failed to delete booking"))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onError(t)
            }
        })
    }

    fun archiveBooking(
        bookingId: Int,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    )
    {
        val token = session.getAccessToken() ?: return onError(Throwable("No token"))
        api.archiveBooking("Bearer $token", bookingId).enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Throwable("Failed to archive booking"))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onError(t)
            }
        })
    }


}
