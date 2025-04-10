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
import com.example.hotelapp.classes.ImageCacheProxy
import com.example.hotelapp.classes.User
import com.example.hotelapp.models.ProfileRequest
import com.example.hotelapp.network.RetrofitClient
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

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
                if (response.isSuccessful) {
                    response.body()?.let { jsonObject ->
                        val avatarUrl = jsonObject.get("avatar_url")?.asString.orEmpty()
                        val fullAvatarUrl = "${apiHolder.BASE_URL}$avatarUrl"

                        val updatedUser = User(
                            id = 0,
                            first_name = jsonObject.get("first_name")?.asString ?: "",
                            last_name = jsonObject.get("last_name")?.asString ?: "",
                            email = jsonObject.get("email")?.asString ?: "",
                            phone = jsonObject.get("phone")?.asString ?: "",
                            birth_date = jsonObject.get("birth_date")?.asString ?: "",
                            avatarUrl = avatarUrl
                        )

                        UserHolder.currentUser = updatedUser
                        sessionManager.saveUserAvatar(fullAvatarUrl)

                        Handler(Looper.getMainLooper()).post {
                            Glide.with(context)
                                .load(fullAvatarUrl)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .placeholder(R.drawable.default_avatar)
                                .into(avatarImageView)

                            avatarImageView.tag = fullAvatarUrl
                        }

                        onSuccess(updatedUser)
                    }
                } else {
                    onError("Failed to load profile: ${response.errorBody()?.string() ?: "Unknown error"}")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onError(t.localizedMessage ?: "Unknown error")
            }
        })
    }




    fun clearAvatarCache() {
        val sessionManager = UserHolder.getSessionManager()
        val token = sessionManager.getAccessToken() ?: return

        userService.getProfileAvatar("Bearer $token").enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val avatarUrl = response.body()?.get("avatar_url")?.asString
                    if (!avatarUrl.isNullOrEmpty()) {
                        val fullAvatarUrl = "${apiHolder.BASE_URL}$avatarUrl"
                        ImageCacheProxy.clearCachedImage(fullAvatarUrl)
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("UserRepository", "Error clearing avatar cache: ${t.message}")
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
                if (response.isSuccessful) {
                    val avatarUrl = response.body()?.get("avatar_url")?.asString
                    Log.d("UserRepository", "Avatar updated: $avatarUrl")

                    if (!avatarUrl.isNullOrEmpty()) {
                        val fullAvatarUrl = "${apiHolder.BASE_URL}$avatarUrl"

                        ImageCacheProxy.clearCachedImage(fullAvatarUrl)

                        ImageCacheProxy.getImage(fullAvatarUrl) { newCachedPath ->
                            Handler(Looper.getMainLooper()).post {
                                Glide.with(context)
                                    .load(newCachedPath)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .placeholder(R.drawable.default_avatar)
                                    .into(avatarImageView)

                                avatarImageView.tag = newCachedPath
                                onSuccess()
                            }
                        }
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to update avatar"
                    Log.e("UserRepository", errorMessage)
                    onError(errorMessage)
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
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val filePath = cursor?.getString(columnIndex ?: 0)
        cursor?.close()
        return filePath
    }

    fun updateCache(user: User) {
        val sessionManager = UserHolder.getSessionManager()
        sessionManager.saveUserData(user)
    }

}