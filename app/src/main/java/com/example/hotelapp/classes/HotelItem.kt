import com.example.hotelapp.classes.RoomItem

data class HotelItem(
    val id: Int,
    val name: String,
    val description: String,
    val images: List<HotelImage>,
    val address: Address,
    val amenities: List<Amenity>
)

data class HotelImage(
    val id: Int,
    val hotel_id: Int,
    val image_url: String
)

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val country: String,
    val postal_code: String,
    val latitude: Double,
    val longitude: Double
)

data class Amenity(
    val id: Int,
    val hotel_id: Int,
    val amenity_id: Int
)