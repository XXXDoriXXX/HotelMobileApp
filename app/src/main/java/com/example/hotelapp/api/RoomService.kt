package com.example.hotelapp.api


import com.example.hotelapp.classes.RoomItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RoomService {
    @GET("rooms/")
    fun getRooms(
        @Query("hotel_id") hotelId: Int
    ): Call<List<RoomItem>>
}