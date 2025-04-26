package com.example.hotelapp.repository

import com.example.hotelapp.api.RoomService
import com.example.hotelapp.classes.RoomItem
import com.example.hotelapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


class RoomRepository {
    private val roomService = RetrofitClient.retrofit.create(RoomService::class.java)

    fun getRooms(hotelId: Int, callback: (List<RoomItem>?, Throwable?) -> Unit) {
        roomService.getRooms(hotelId).enqueue(object : retrofit2.Callback<List<RoomItem>> {
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
