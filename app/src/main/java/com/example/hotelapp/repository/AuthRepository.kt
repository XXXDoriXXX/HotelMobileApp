package com.example.hotelapp.repository

import com.example.hotelapp.api.AuthService
import com.example.hotelapp.models.*
import com.example.hotelapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepository {
    private val authService = RetrofitClient.retrofit.create(AuthService::class.java)

    fun registerUser(request: RegisterRequest): Call<AuthResponse> {
        return authService.registerUser(request)
    }

    fun loginUser(request: LoginRequest): Call<AuthResponse> {

        return authService.loginUser(request)
    }



}
