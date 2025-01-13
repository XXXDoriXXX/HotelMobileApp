package com.example.hotelapp.models

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token") val token: String,
    @SerializedName("token_type") val tokenType: String
)
