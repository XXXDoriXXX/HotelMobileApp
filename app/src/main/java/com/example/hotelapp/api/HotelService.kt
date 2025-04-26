package com.example.hotelapp.api
import HotelImage
import HotelItem
import HotelResponseWrapper
import RatingRequest
import RatingResponse
import ViewResponse
import com.example.hotelapp.models.BookingRequest
import com.example.hotelapp.models.BookingResponse
import com.example.hotelapp.models.HotelSearchParams
import com.example.hotelapp.models.HotelWithStatsResponse
import com.example.hotelapp.models.PaymentRequest
import com.example.hotelapp.models.PaymentSuccessRequest
import com.example.hotelapp.models.PaymentSuccessResponse
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
    @GET("hotels/")
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
    @POST("stripe/payment-success")
    fun notifyPaymentSuccess(@Body paymentSuccessRequest: PaymentSuccessRequest): Call<PaymentSuccessResponse>
    @GET("hotels/trending")
    fun getTrendingHotels(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("skip") skip: Int,
        @Query("limit") limit: Int
    ): Call<List<HotelResponseWrapper>>

    @GET("hotels/best-deals")
    fun getBestDeals(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("skip") skip: Int,
        @Query("limit") limit: Int
    ): Call<List<HotelResponseWrapper>>

    @GET("hotels/popular")
    fun getPopularHotels(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("skip") skip: Int,
        @Query("limit") limit: Int
    ): Call<List<HotelResponseWrapper>>
    @GET("hotels/{hotel_id}")
    fun getHotelWithStats(
        @Path("hotel_id") hotelId: Int,
        @Header("Authorization") token: String
    ): Call<HotelWithStatsResponse>
    @PUT("hotels/{hotel_id}/rate")
    fun rateHotelPut(
        @Path("hotel_id") hotelId: Int,
        @Body value: Float,
        @Header("Authorization") token: String
    ): Call<Void>
    @POST("hotels/search")
    fun searchHotels(
        @Body filters: HotelSearchParams
    ): Call<List<HotelResponseWrapper>>

    @POST("/bookings/checkout")
    fun createBookingCheckout(
        @Body bookingRequest: BookingRequest,
        @Header("Authorization") token: String
    ): Call<StripePaymentResponse>


}
