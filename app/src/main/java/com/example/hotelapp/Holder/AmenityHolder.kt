package com.example.hotelapp.Holder

import com.example.hotelapp.classes.Amenity

object AmenityHolder {
    var allAmenities: List<Amenity> = emptyList()

    fun getById(id: Int): Amenity? {
        return allAmenities.find { it.id == id }
    }
}