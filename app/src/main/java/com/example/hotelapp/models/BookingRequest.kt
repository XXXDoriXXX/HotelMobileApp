package com.example.hotelapp.models

data class BookingRequest(
    val hotel_id: Int,
    val room_id: Int,
    val user_id: Int,
    val check_in: String,
    val check_out: String,
    val total_price: Float
)
