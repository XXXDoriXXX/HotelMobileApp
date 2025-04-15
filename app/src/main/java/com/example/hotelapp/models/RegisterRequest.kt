package com.example.hotelapp.models

data class RegisterRequest(
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone: String,
    val password: String,
    val birth_date: String
)
