package com.example.hotelapp.models

data class HotelSearchParams(
    val city: String? = null,
    val country: String? = null,
    val min_price: Float? = null,
    val max_price: Float? = null,
    val min_rating: Float? = null,
    val room_type: String? = null,
    val amenity_ids: List<Int>? = null,
    val check_in: String? = null, // ISO-8601 format: "2024-05-01"
    val check_out: String? = null,
    val sort_by: String? = null,  // "rating", "price", "views"
    val sort_dir: String? = null, // "asc" or "desc"
    val skip: Int = 0,
    val limit: Int = 25
)
