package com.example.hotelapp.models

data class PaymentRequest(
    val amount: Float
)

data class StripePaymentResponse(
    val id: String,
    val clientSecret: String,
    val status: String
)
