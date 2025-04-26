package com.example.hotelapp.classes

data class OrderItem(
    val hotelName: String,
    val roomType: String,
    val checkInDate: String,
    val checkOutDate: String,
    val totalPrice: Float,
    var  status: String
)
