package com.example.hotelapp.models

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    val amount: Float
)

data class StripePaymentResponse(
    @SerializedName("checkout_url")
    val checkoutUrl: String
)