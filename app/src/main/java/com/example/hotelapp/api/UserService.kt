package com.example.hotelapp.api

import com.example.hotelapp.classes.User
import com.example.hotelapp.models.ProfileRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT

interface UserService {
    @PUT("profile/")
    fun updateUserProfile(
        @Body request: ProfileRequest,
        @Header("Authorization") token: String
    ): Call<Void>
}