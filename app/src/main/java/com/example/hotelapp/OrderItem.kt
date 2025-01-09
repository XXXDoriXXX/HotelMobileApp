package com.example.hotelapp

data class OrderItem(
    val hotelName: String,
    val roomType: String,
    val checkInDate: String,
    val checkOutDate: String,
    val totalPrice: Float
)
