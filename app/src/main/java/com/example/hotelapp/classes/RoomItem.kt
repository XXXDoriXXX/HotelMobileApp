package com.example.hotelapp.classes

data class RoomItem(
    val id: Int,
    val room_number: String,
    val room_type: String,
    val places: Int,
    val price_per_night: Float,
    val images: List<RoomImage>,
    var description:String
)

data class RoomImage(
    val id: Int,
    val image_url: String
)