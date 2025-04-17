package com.example.hotelapp.models

import HotelItem

data class HotelWithStatsResponse(
    val hotel: HotelItem,
    val rating: Float,
    val views: Int
)
