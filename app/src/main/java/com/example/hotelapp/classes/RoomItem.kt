package com.example.hotelapp.classes

data class RoomItem(
    val id: Int,
    val room_number: String,
    val room_type: String,
    val places: Int,
    val price_per_night: Float,
    val description: String,
    val images: List<RoomImage>,
    val amenities: List<RoomAmenity>
)

data class RoomImage(
    val id: Int,
    val room_id: Int,
    val image_url: String
)

data class RoomAmenity(
    val id: Int,
    val room_id: Int,
    val amenity_id: Int
)
