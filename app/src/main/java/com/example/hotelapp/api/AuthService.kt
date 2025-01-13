package com.example.hotelapp.api

import com.example.hotelapp.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST("auth/register")
    fun registerUser(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("auth/login")
    fun loginUser(@Body request: LoginRequest): Call<AuthResponse>

}
