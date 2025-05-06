import com.example.hotelapp.api.HotelService
import com.example.hotelapp.classes.Amenity
import com.example.hotelapp.classes.ErrorUtils
import com.example.hotelapp.models.FavoriteHotelWrapper
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
                    onError(Exception(ErrorUtils.parseErrorMessage(response)))
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
                    onError(Exception(ErrorUtils.parseErrorMessage(response)))
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
                        onError(Exception(ErrorUtils.parseErrorMessage(response)))

                    }
                }

                override fun onFailure(call: Call<HotelWithStatsResponse>, t: Throwable) {
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
                    onError(Exception(ErrorUtils.parseErrorMessage(response)))
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
        onResult: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val token = sessionManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            onError(Exception("No access token available"))
            return
        }

        apiService.rateHotel(hotelId, rating, "Bearer $token")
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        onResult()
                    } else {
                        val error = response.errorBody()?.string()
                        onError(Exception(ErrorUtils.parseErrorMessage(response)))

                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    onError(t)
                }
            })
    }
    fun getFavorites(
        onResult: (List<HotelItem>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val token = sessionManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            onError(Exception("No access token"))
            return
        }

        apiService.getFavorites("Bearer $token").enqueue(object : Callback<List<FavoriteHotelWrapper>> {
            override fun onResponse(
                call: Call<List<FavoriteHotelWrapper>>,
                response: Response<List<FavoriteHotelWrapper>>
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
                    onError(Exception(ErrorUtils.parseErrorMessage(response)))

                }
            }

            override fun onFailure(call: Call<List<FavoriteHotelWrapper>>, t: Throwable) {
                onError(t)
            }
        })
    }




    fun addFavorite(
        hotelId: Int,
        onResult: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val token = sessionManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            onError(Exception("No access token"))
            return
        }
        apiService.addFavorite(hotelId, "Bearer $token").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) onResult()
                else onError(Exception(ErrorUtils.parseErrorMessage(response)))

            }

            override fun onFailure(call: Call<Void>, t: Throwable) = onError(t)
        })
    }

    fun removeFavorite(
        hotelId: Int,
        onResult: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val token = sessionManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            onError(Exception("No access token"))
            return
        }
        apiService.removeFavorite(hotelId, "Bearer $token").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) onResult()
                else onError(Exception(ErrorUtils.parseErrorMessage(response)))

            }

            override fun onFailure(call: Call<Void>, t: Throwable) = onError(t)
        })
    }
    fun getAllAmenities(
        onResult: (List<Amenity>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        var hotelAmenities: List<Amenity>? = null
        var roomAmenities: List<Amenity>? = null
        var errorOccurred = false

        val checkAndReturn = {
            if (hotelAmenities != null && roomAmenities != null) {
                val combined = (hotelAmenities!! + roomAmenities!!).distinctBy { it.id }
                onResult(combined)
            }
        }

        apiService.getHotelAmenities().enqueue(object : Callback<List<Amenity>> {
            override fun onResponse(call: Call<List<Amenity>>, response: Response<List<Amenity>>) {
                if (response.isSuccessful) {
                    hotelAmenities = response.body() ?: emptyList()
                    checkAndReturn()
                } else if (!errorOccurred) {
                    errorOccurred = true
                    onError(Exception("Failed to fetch hotel amenities"))
                }
            }

            override fun onFailure(call: Call<List<Amenity>>, t: Throwable) {
                if (!errorOccurred) {
                    errorOccurred = true
                    onError(t)
                }
            }
        })

        apiService.getRoomAmenities().enqueue(object : Callback<List<Amenity>> {
            override fun onResponse(call: Call<List<Amenity>>, response: Response<List<Amenity>>) {
                if (response.isSuccessful) {
                    roomAmenities = response.body() ?: emptyList()
                    checkAndReturn()
                } else if (!errorOccurred) {
                    errorOccurred = true
                    onError(Exception("Failed to fetch room amenities"))
                }
            }

            override fun onFailure(call: Call<List<Amenity>>, t: Throwable) {
                if (!errorOccurred) {
                    errorOccurred = true
                    onError(t)
                }
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
                    onError(Exception(ErrorUtils.parseErrorMessage(response)))
                }
            }

            override fun onFailure(call: Call<List<HotelItem>>, t: Throwable) {
                onError(t)
            }
        })
    }

}
