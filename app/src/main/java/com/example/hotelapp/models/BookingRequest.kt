package com.example.hotelapp.models

data class BookingRequest(
    val room_id: Int,
    val date_start: String,
    val date_end: String,
    val payment_method: String
)
