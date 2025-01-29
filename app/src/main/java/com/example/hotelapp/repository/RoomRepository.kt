package com.example.hotelapp.repository

import com.example.hotelapp.classes.RoomItem
import com.example.hotelapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RoomService {
    @GET("rooms/search")
    fun searchRooms(
        @Query("hotel_id") hotelId: Int,
        @Query("room_number") roomNumber: String
    ): Call<List<RoomItem>>
}

class RoomRepository {
    private val roomService = RetrofitClient.retrofit.create(RoomService::class.java)

    fun searchRooms(hotelId: Int, roomNumber: String, callback: (List<RoomItem>?, Throwable?) -> Unit) {
        roomService.searchRooms(hotelId, roomNumber).enqueue(object : retrofit2.Callback<List<RoomItem>> {
            override fun onResponse(call: Call<List<RoomItem>>, response: retrofit2.Response<List<RoomItem>>) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    callback(null, Throwable(response.message()))
                }
            }

            override fun onFailure(call: Call<List<RoomItem>>, t: Throwable) {
                callback(null, t)
            }
        })
    }
}
