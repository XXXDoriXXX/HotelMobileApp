package com.example.hotelapp.api
import HotelImage
import HotelItem
import RatingRequest
import RatingResponse
import ViewResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

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
}