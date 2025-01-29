package com.example.hotelapp.repository
import com.example.hotelapp.api.UserService
import com.example.hotelapp.classes.User
import com.example.hotelapp.models.ProfileRequest
import com.example.hotelapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository {
    private val userService = RetrofitClient.retrofit.create(UserService::class.java)

    fun updateUserProfile(user: User, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val sessionManager = UserHolder.getSessionManager()
        val token = sessionManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            onError("No access token available")
            return
        }

        val profileRequest = ProfileRequest(
            first_name = user.first_name,
            last_name = user.last_name,
            phone = user.phone,
            birth_date = user.birth_date
        )

        userService.updateUserProfile(profileRequest, "Bearer $token").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Failed to update profile: ${response.errorBody()?.string() ?: "Unknown error"}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onError(t.localizedMessage ?: "Unknown error")
            }
        })
    }

}