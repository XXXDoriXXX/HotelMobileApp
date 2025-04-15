package com.example.hotelapp.models

data class ChangeCredentialsRequest(
    val current_password: String,
    val confirm_password: String,
    val new_password: String? = null,
    val new_email: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val phone: String? = null,
    val birth_date: String? = null
)