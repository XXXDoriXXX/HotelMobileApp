package com.example.hotelapp.models

data class BookingResponse(
    val id: Int,
    val client_id: Int,
    val room_id: Int,
    val date_start: String,
    val date_end: String,
    val total_price: Float
)