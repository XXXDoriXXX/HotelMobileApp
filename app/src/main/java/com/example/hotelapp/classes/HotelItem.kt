import com.example.hotelapp.classes.RoomItem

data class HotelItem(
    val id: Int,
    val name: String,
    val description: String,
    val images: List<HotelImage>,
    val address: Address,
    val amenities: List<Amenity>,
    var rating: Float = 0f,
    var is_card_available: Boolean,
    var views: Int = 0
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
data class HotelResponseWrapper(
    val hotel: HotelItem,
    val rating: Float,
    val views: Int
)
