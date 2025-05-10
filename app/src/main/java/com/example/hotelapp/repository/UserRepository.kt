package com.example.hotelapp.repository

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.hotelapp.Holder.apiHolder
import com.example.hotelapp.R
import com.example.hotelapp.api.UserService
import com.example.hotelapp.classes.User
import com.example.hotelapp.models.ProfileRequest
import com.example.hotelapp.network.RetrofitClient
import com.google.gson.JsonObject
import com.google.gson.JsonNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import com.example.hotelapp.models.ChangeCredentialsRequest


class UserRepository {
    private val userService = RetrofitClient.retrofit.create(UserService::class.java)

    fun updateCredentials(
        context: Context,
        request: ChangeCredentialsRequest,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        val sessionManager = UserHolder.getSessionManager()
        val token = sessionManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            onError("No access token available")
            return
        }

        val service = RetrofitClient.retrofit.create(UserService::class.java)
        service.updateCredentials(request, "Bearer $token").enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    val updatedUser = response.body()!!
                    UserHolder.currentUser = updatedUser
                    sessionManager.saveUserData(updatedUser)
                    onSuccess(updatedUser)
                } else {
                    val msg = response.errorBody()?.string() ?: "Unknown error"
                    onError("Update failed: $msg")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                onError(t.localizedMessage ?: "Unknown error")
            }
        })
    }
    fun loadProfile(context: Context, onSuccess: (User) -> Unit, onError: (Throwable) -> Unit) {
        val sessionManager = UserHolder.getSessionManager()
        val token = sessionManager.getAccessToken() ?: return onError(Throwable("No access token available"))

        userService.getProfileDetails("Bearer $token").enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                try {
                    if (response.isSuccessful && response.body() != null) {
                        val json = response.body()!!
                        val user = User(
                            id = json["id"]?.asInt ?: 0,
                            first_name = json["first_name"]?.asString ?: "",
                            last_name = json["last_name"]?.asString ?: "",
                            email = json["email"]?.asString ?: "",
                            phone = json["phone"]?.asString ?: "",
                            birth_date = json["birth_date"]?.asString ?: "",
                            avatarUrl = json["avatar_url"]?.asString ?: ""
                        )
                        UserHolder.currentUser = user
                        sessionManager.saveUserData(user)
                        onSuccess(user)
                    } else {
                        onError(Throwable("Failed to parse profile response"))
                    }
                } catch (e: Exception) {
                    onError(Throwable("Parsing error: ${e.message}"))
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onError(t)
            }
        })
    }

    fun loadProfileDetails(
        context: Context,
        avatarImageView: ImageView,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        val sessionManager = UserHolder.getSessionManager()
        val token = sessionManager.getAccessToken() ?: return onError("No access token available")

        userService.getProfileDetails("Bearer $token").enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                try {
                    if (response.isSuccessful) {
                        response.body()?.let { jsonObject ->
                            val avatarUrl = jsonObject.get("avatar_url")?.takeUnless { it is JsonNull }?.asString.orEmpty()
                            val fullAvatarUrl = if (avatarUrl.isNotEmpty()) avatarUrl else ""

                            val favoriteIdsJson = jsonObject.get("favorite_hotel_ids")
                            val favoriteIds = if (favoriteIdsJson != null && favoriteIdsJson.isJsonArray) {
                                favoriteIdsJson.asJsonArray.mapNotNull { it.asInt }
                            } else {
                                emptyList()
                            }

                            val updatedUser = User(
                                id = jsonObject.get("id")?.asInt ?: 0,
                                first_name = jsonObject.get("first_name")?.asString ?: "",
                                last_name = jsonObject.get("last_name")?.asString ?: "",
                                email = jsonObject.get("email")?.asString ?: "",
                                phone = jsonObject.get("phone")?.asString ?: "",
                                birth_date = jsonObject.get("birth_date")?.asString ?: "",
                                avatarUrl = avatarUrl
                            )

                            sessionManager.saveFavoriteHotelIds(favoriteIds)
                            UserHolder.currentUser = updatedUser
                            sessionManager.saveUserAvatar(fullAvatarUrl)

                            Handler(Looper.getMainLooper()).post {
                                if (fullAvatarUrl.isNotEmpty()) {
                                    Glide.with(context)
                                        .load(fullAvatarUrl)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .placeholder(R.drawable.default_avatar)
                                        .into(avatarImageView)

                                    avatarImageView.tag = fullAvatarUrl
                                } else {
                                    Glide.with(context)
                                        .load(R.drawable.default_avatar)
                                        .into(avatarImageView)
                                }
                            }

                            onSuccess(updatedUser)
                        } ?: onError("Empty response body")
                    } else {
                        onError("Failed to load profile: ${response.errorBody()?.string() ?: "Unknown error"}")
                    }
                } catch (e: Exception) {
                    onError("Error parsing response: ${e.message}")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onError(t.localizedMessage ?: "Unknown error")
            }
        })
    }

    fun uploadNewAvatar(context: Context, uri: Uri, avatarImageView: ImageView, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val sessionManager = UserHolder.getSessionManager()
        val token = sessionManager.getAccessToken() ?: return onError("No access token available")

        val filePath = getPathFromUri(context, uri) ?: return onError("Failed to get file path")
        val file = File(filePath)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        userService.changeAvatar("Bearer $token", body).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                try {
                    if (response.isSuccessful) {
                        val avatarUrl = response.body()?.get("avatar_url")?.takeUnless { it is JsonNull }?.asString
                        Log.d("UserRepository", "Avatar updated: $avatarUrl")

                        if (!avatarUrl.isNullOrEmpty()) {
                            val fullAvatarUrl = "${apiHolder.BASE_URL}$avatarUrl"

                            Handler(Looper.getMainLooper()).post {
                                Glide.with(context)
                                    .load(fullAvatarUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .placeholder(R.drawable.default_avatar)
                                    .into(avatarImageView)

                                avatarImageView.tag = fullAvatarUrl
                                onSuccess()
                            }
                        } else {
                            onSuccess()
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Failed to update avatar"
                        Log.e("UserRepository", errorMessage)
                        onError(errorMessage)
                    }
                } catch (e: Exception) {
                    onError("Error processing avatar update: ${e.message}")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("UserRepository", "Avatar upload failed: ${t.message}")
                onError(t.message ?: "Unknown error")
            }
        })
    }

    private fun getPathFromUri(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    fun updateCache(user: User) {
        val sessionManager = UserHolder.getSessionManager()
        sessionManager.saveUserData(user)
    }
}