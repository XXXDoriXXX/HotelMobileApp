import com.example.hotelapp.api.HotelService
import com.example.hotelapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HotelRepository(private val apiService: HotelService,private val sessionManager: SessionManager) {
    fun getHotels(onResult: (List<HotelItem>) -> Unit, onError: (Throwable) -> Unit) {
        apiService.getAllHotelsWithDetails().enqueue(object : Callback<List<HotelItem>> {
            override fun onResponse(call: Call<List<HotelItem>>, response: Response<List<HotelItem>>) {
                if (response.isSuccessful) {
                    onResult(response.body() ?: emptyList())
                } else {
                    onError(Exception("Failed to fetch hotels: ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<List<HotelItem>>, t: Throwable) {
                onError(t)
            }
        })
    }
    fun incrementViews(hotelId: Int, onResult: (ViewResponse) -> Unit, onError: (Throwable) -> Unit) {
        val token = sessionManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            onError(Exception("No access token available"))
            return
        }

        apiService.incrementViews(hotelId, "Bearer $token").enqueue(object : retrofit2.Callback<ViewResponse> {
            override fun onResponse(call: Call<ViewResponse>, response: Response<ViewResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let(onResult)
                } else {
                    onError(Exception("Failed to increment views: ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<ViewResponse>, t: Throwable) {
                onError(t)
            }
        })
    }
    fun rateHotel(
        hotelId: Int,
        rating: Float,
        onResult: (RatingResponse) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val token = sessionManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            onError(Exception("No access token available"))
            return
        }

        val request = RatingRequest(rating)
        apiService.rateHotel(hotelId, request, "Bearer $token").enqueue(object : retrofit2.Callback<RatingResponse> {
            override fun onResponse(call: Call<RatingResponse>, response: Response<RatingResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let(onResult)
                } else {
                    onError(Exception("Failed to rate hotel: ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<RatingResponse>, t: Throwable) {
                onError(t)
            }
        })
    }
    fun searchHotels(
        name: String,
        onResult: (List<HotelItem>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        apiService.searchHotels(name).enqueue(object : Callback<List<HotelItem>> {
            override fun onResponse(call: Call<List<HotelItem>>, response: Response<List<HotelItem>>) {
                if (response.isSuccessful) {
                    onResult(response.body() ?: emptyList())
                } else {
                    onError(Exception("Failed to search hotels"))
                }
            }

            override fun onFailure(call: Call<List<HotelItem>>, t: Throwable) {
                onError(t)
            }
        })
    }

}
