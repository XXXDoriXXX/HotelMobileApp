import com.example.hotelapp.api.HotelService
import com.example.hotelapp.models.HotelSearchParams
import com.example.hotelapp.models.HotelWithStatsResponse
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
    fun getHotelsByCategory(
        category: String,
        city: String,
        country: String,
        skip: Int,
        limit: Int,
        onResult: (List<HotelItem>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val call = when (category) {
            "trending" -> apiService.getTrendingHotels(city, country, skip, limit)
            "best" -> apiService.getBestDeals(city, country, skip, limit)
            "popular" -> apiService.getPopularHotels(city, country, skip, limit)
            else -> return onError(Exception("Unknown category"))
        }

        call.enqueue(object : Callback<List<HotelResponseWrapper>> {
            override fun onResponse(
                call: Call<List<HotelResponseWrapper>>,
                response: Response<List<HotelResponseWrapper>>
            ) {
                if (response.isSuccessful) {
                    val hotels = response.body()?.map {
                        it.hotel.apply {
                            rating = it.rating
                            views = it.views
                        }
                    } ?: emptyList()
                    onResult(hotels)
                } else {
                    onError(Exception("Failed to fetch hotels: ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<List<HotelResponseWrapper>>, t: Throwable) {
                onError(t)
            }
        })

    }
    fun getHotelById(
        hotelId: Int,
        onResult: (HotelItem) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val token = sessionManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            onError(Exception("No access token available"))
            return
        }

        apiService.getHotelWithStats(hotelId, "Bearer $token")
            .enqueue(object : Callback<HotelWithStatsResponse> {
                override fun onResponse(
                    call: Call<HotelWithStatsResponse>,
                    response: Response<HotelWithStatsResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            body.hotel.rating = body.rating
                            body.hotel.views = body.views
                            onResult(body.hotel)
                        } else onError(Exception("Empty response"))
                    } else {
                        onError(Exception("Failed to fetch hotel: ${response.message()}"))
                    }
                }

                override fun onFailure(call: Call<HotelWithStatsResponse>, t: Throwable) {
                    onError(t)
                }
            })
    }

    fun rateHotelPUT(
        hotelId: Int,
        ratingValue: Float,
        onResult: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val token = sessionManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            onError(Exception("No access token"))
            return
        }

        apiService.rateHotelPut(hotelId, ratingValue, "Bearer $token")
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        onResult()
                    } else {
                        onError(Exception("Rating failed: ${response.message()}"))
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    onError(t)
                }
            })
    }

    fun searchHotelsByFilters(
        filters: HotelSearchParams,
        onResult: (List<HotelItem>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        apiService.searchHotels(filters).enqueue(object : Callback<List<HotelResponseWrapper>> {
            override fun onResponse(
                call: Call<List<HotelResponseWrapper>>,
                response: Response<List<HotelResponseWrapper>>
            ) {
                if (response.isSuccessful) {
                    val hotels = response.body()?.map {
                        it.hotel.apply {
                            rating = it.rating
                            views = it.views
                        }
                    } ?: emptyList()
                    onResult(hotels)
                } else {
                    onError(Exception("Search failed: ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<List<HotelResponseWrapper>>, t: Throwable) {
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
