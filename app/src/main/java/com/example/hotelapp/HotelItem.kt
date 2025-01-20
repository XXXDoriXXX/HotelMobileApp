import com.example.hotelapp.RoomItem

data class HotelItem(
    val id: Int,
    val name: String,
    val address: String,
    val images: List<HotelImage>,
    val rooms: List<RoomItem>,
    var views:Int,
    var rating:Float,
    var description:String
)

data class HotelImage(
    val id: Int,
    val image_url: String
)