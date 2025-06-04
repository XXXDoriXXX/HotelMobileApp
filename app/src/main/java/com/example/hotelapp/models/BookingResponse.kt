package com.example.hotelapp.models

data class BookingResponse(
    val booking_id: Int,
    val hotel_name: String,
    val room_type: String,
    val date_start: String,
    val date_end: String,
    val total_price: Float,
    val status: String,
    val hotel_images: List<HotelImage>?,
    val room_id:Int,
    val created_at: String

)
data class HotelImage(
    val id: Int,
    val hotel_id: Int,
    val image_url: String
)
