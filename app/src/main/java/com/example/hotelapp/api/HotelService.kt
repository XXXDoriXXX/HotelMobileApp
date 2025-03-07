package com.example.hotelapp.api
import HotelImage
import HotelItem
import RatingRequest
import RatingResponse
import ViewResponse
import com.example.hotelapp.models.BookingRequest
import com.example.hotelapp.models.BookingResponse
import com.example.hotelapp.models.PaymentRequest
import com.example.hotelapp.models.StripePaymentResponse
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface HotelService {
    @GET("hotels/all_details")
    fun getAllHotelsWithDetails(): Call<List<HotelItem>>
    @POST("hotels/{hotel_id}/view")
    fun incrementViews(
        @Path("hotel_id") hotelId: Int,
        @Header("Authorization") token: String
    ): Call<ViewResponse>
    @POST("hotels/{hotel_id}/rate")
    fun rateHotel(
        @Path("hotel_id") hotelId: Int,
        @Body ratingRequest: RatingRequest,
        @Header("Authorization") token: String
    ): Call<RatingResponse>
    @GET("hotels/search")
    fun searchHotels(@Query("name") name: String): Call<List<HotelItem>>
    @POST("/bookings/")
    fun createBooking(@Body bookingRequest: BookingRequest): Call<BookingResponse>
    @POST("stripe/create-payment-intent")
    fun createPaymentIntent(@Body paymentRequest: PaymentRequest): Call<StripePaymentResponse>


}