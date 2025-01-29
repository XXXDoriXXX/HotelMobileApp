package com.example.hotelapp.models

data class ProfileRequest (
    val first_name: String,
    val last_name: String,
    val phone: String,
    val birth_date: String
)