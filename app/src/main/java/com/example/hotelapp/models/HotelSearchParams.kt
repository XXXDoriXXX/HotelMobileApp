package com.example.hotelapp.models

data class HotelSearchParams(
    val name: String? = null,
    val description: String? = null,
    val city: String? = null,
    val country: String? = null,
    val state: String? = null,
    val postal_code: String? = null,
    val min_price: Float? = null,
    val max_price: Float? = null,
    val min_rating: Float? = null,
    val room_type: String? = null,
    val amenity_ids: List<Int>? = null,
    val check_in: String? = null,
    val check_out: String? = null,
    val sort_by: String = "rating",
    val sort_dir: String = "desc",
    val skip: Int = 0,
    val limit: Int = 25
)
