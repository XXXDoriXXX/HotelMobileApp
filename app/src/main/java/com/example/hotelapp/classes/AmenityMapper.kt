package com.example.hotelapp.classes

import android.content.Context
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
    private val nameResMap = mapOf(
        "Free WiFi" to R.string.amenity_wifi,
        "Parking" to R.string.amenity_parking,
        "Swimming Pool" to R.string.amenity_pool,
        "Breakfast Included" to R.string.amenity_breakfast,
        "Fitness Center" to R.string.amenity_gym,
        "Spa and Wellness Center" to R.string.amenity_spa,
        "24-Hour Front Desk" to R.string.amenity_front_desk,
        "Elevator" to R.string.amenity_elevator,
        "Concierge Service" to R.string.amenity_concierge,
        "Luggage Storage" to R.string.amenity_luggage,
        "Daily Housekeeping" to R.string.amenity_cleaning,
        "Airport Shuttle" to R.string.amenity_shuttle,
        "Meeting Facilities" to R.string.amenity_meeting,
        "Pet Friendly" to R.string.amenity_pet,
        "Wheelchair Accessible" to R.string.amenity_wheelchair,
        "Non-Smoking Rooms" to R.string.amenity_no_smoking,
        "Smoke-Free Property" to R.string.amenity_no_smoking,
        "Business Center" to R.string.amenity_business,
        "Bicycle Rental" to R.string.amenity_bicycle,
        "Terrace" to R.string.amenity_terrace,
        "Laundry Service" to R.string.amenity_laundry,
        "Air Conditioning" to R.string.amenity_ac,
        "TV in Room" to R.string.amenity_tv,
        "Mini-Bar" to R.string.amenity_minibar,
        "Room Service" to R.string.amenity_room_service,
        "Private Bathroom" to R.string.amenity_bathroom,
        "Desk" to R.string.amenity_desk,
        "Hairdryer" to R.string.amenity_hairdryer,
        "Electric Kettle" to R.string.amenity_kettle,
        "Iron and Ironing Board" to R.string.amenity_iron,
        "Soundproofing" to R.string.amenity_soundproof,
        "Safe" to R.string.amenity_safe,
        "Balcony" to R.string.amenity_balcony,
        "Towels and Linens" to R.string.amenity_towels,
        "Clothes Rack" to R.string.amenity_clothes
    )

    fun mapAmenity(context: Context, id: Int): AmenityDisplay {
        val amenity = AmenityHolder.getById(id)
        val rawName = amenity?.name ?: "Other"
        val name = context.getString(nameResMap[rawName] ?: R.string.amenity_other)
        val icon = iconMap[rawName] ?: R.drawable.other
        return AmenityDisplay(icon, name)
    }

}
