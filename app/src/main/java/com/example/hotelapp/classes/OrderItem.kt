package com.example.hotelapp.classes

data class OrderItem(
    val bookingId: Int,
    val hotelName: String,
    val room_id: Int,
    val roomType: String,
    val checkInDate: String,
    val checkOutDate: String,
    val totalPrice: Float,
    var status: String,
    val hotel_image_url: String?,
    val createdAt: String
)