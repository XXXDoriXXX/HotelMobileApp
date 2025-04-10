package com.example.hotelapp.models

data class PaymentSuccessResponse(
    val message: String,
    val booking_id: Int,
    val payment_db_id: Int
)