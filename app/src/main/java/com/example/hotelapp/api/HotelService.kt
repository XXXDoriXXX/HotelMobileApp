package com.example.hotelapp.api
import HotelImage
import HotelItem
import HotelResponseWrapper
import RatingRequest
import RatingResponse
import ViewResponse
import com.example.hotelapp.models.BookingRequest
import com.example.hotelapp.models.BookingResponse
import com.example.hotelapp.models.FavoriteHotelWrapper
import com.example.hotelapp.models.HotelSearchParams
import com.example.hotelapp.models.HotelWithStatsResponse
import com.example.hotelapp.models.PaymentRequest
import com.example.hotelapp.models.PaymentSuccessRequest
import com.example.hotelapp.models.PaymentSuccessResponse
import com.example.hotelapp.models.RefundResponse
import com.example.hotelapp.models.StripePaymentResponse
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
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
    @PUT("hotels/{hotel_id}/rate")
    fun rateHotel(
        @Path("hotel_id") hotelId: Int,
        @Body rating: Float,
        @Header("Authorization") token: String
    ): Call<Void>

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
    @POST("hotels/search")
    fun searchHotels(
        @Body filters: HotelSearchParams
    ): Call<List<HotelResponseWrapper>>

    @POST("/bookings/checkout")
    fun createBookingCheckout(
        @Body bookingRequest: BookingRequest,
        @Header("Authorization") token: String
    ): Call<StripePaymentResponse>

    @GET("bookings/my")
    fun getMyBookingsSorted(
        @Header("Authorization") token: String,
        @Query("sort_by") sortBy: String,
        @Query("order") order: String
    ): Call<List<BookingResponse>>

    @POST("/bookings/{booking_id}/refund-request")
    fun requestRefund(
        @Path("booking_id") bookingId: Int,
        @Header("Authorization") token: String
    ): Call<RefundResponse>
    @POST("bookings/checkout")
    fun createBookingCheckoutRaw(
        @Body request: BookingRequest,
        @Header("Authorization") token: String
    ): Call<JsonObject>
    @GET("favorites/")
    fun getFavorites(@Header("Authorization") token: String): Call<List<FavoriteHotelWrapper>>
    @POST("favorites/{hotel_id}")
    fun addFavorite(
        @Path("hotel_id") hotelId: Int,
        @Header("Authorization") token: String
    ): Call<Void>

    @DELETE("favorites/{hotel_id}")
    fun removeFavorite(
        @Path("hotel_id") hotelId: Int,
        @Header("Authorization") token: String
    ): Call<Void>
    @DELETE("bookings/{id}")
    fun deleteBooking(@Header("Authorization") token: String, @Path("id") id: Int): Call<ResponseBody>

    @POST("bookings/{id}/archive")
    fun archiveBooking(@Header("Authorization") token: String, @Path("id") id: Int): Call<ResponseBody>

}
