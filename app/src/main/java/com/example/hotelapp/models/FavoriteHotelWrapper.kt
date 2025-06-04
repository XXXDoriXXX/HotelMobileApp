package com.example.hotelapp.models

import HotelItem

data class FavoriteHotelWrapper(
    val hotel: HotelItem,
    val rating: Float,
    val views: Int
)
