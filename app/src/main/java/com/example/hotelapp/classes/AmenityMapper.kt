package com.example.hotelapp.classes

import com.example.hotelapp.Holder.AmenityHolder
import com.example.hotelapp.R
import com.example.hotelapp.classes.Adapters.AmenityDisplay

object AmenityMapper {
    private val iconMap = mapOf(
        "Free WiFi" to R.drawable.wifi,
        "Parking" to R.drawable.parking,
        "Swimming Pool" to R.drawable.pool,
        "Breakfast Included" to R.drawable.breakfast,
        "Fitness Center" to R.drawable.gym,
        "Spa and Wellness Center" to R.drawable.spa,
        "24-Hour Front Desk" to R.drawable.front_desk,
        "Elevator" to R.drawable.elevator,
        "Concierge Service" to R.drawable.concierge,
        "Luggage Storage" to R.drawable.luggage,
        "Daily Housekeeping" to R.drawable.cleaning,
        "Airport Shuttle" to R.drawable.shuttle,
        "Meeting Facilities" to R.drawable.meeting,
        "Pet Friendly" to R.drawable.pet,
        "Wheelchair Accessible" to R.drawable.wheelchair,
        "Non-Smoking Rooms" to R.drawable.no_smoking,
        "Smoke-Free Property" to R.drawable.no_smoking,
        "Business Center" to R.drawable.business,
        "Bicycle Rental" to R.drawable.bicycle,
        "Terrace" to R.drawable.other,
        "Laundry Service" to R.drawable.other,

        "Air Conditioning" to R.drawable.ac,
        "TV in Room" to R.drawable.tv,
        "Mini-Bar" to R.drawable.other,
        "Room Service" to R.drawable.concierge,
        "Private Bathroom" to R.drawable.bathroom,
        "Desk" to R.drawable.desk,
        "Hairdryer" to R.drawable.ic_other,
        "Electric Kettle" to R.drawable.other,
        "Iron and Ironing Board" to R.drawable.other,
        "Soundproofing" to R.drawable.soundproof,
        "Safe" to R.drawable.safe,
        "Balcony" to R.drawable.other,
        "Towels and Linens" to R.drawable.other,
        "Clothes Rack" to R.drawable.clothes
    )

    fun mapAmenity(id: Int): AmenityDisplay {
        val amenity = AmenityHolder.getById(id)
        val name = amenity?.name ?: "Other"
        val icon = iconMap[name] ?: R.drawable.other
        return AmenityDisplay(icon, name)
    }
}
