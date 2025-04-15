package com.example.hotelapp.api

import com.example.hotelapp.classes.User
import com.example.hotelapp.models.ChangeCredentialsRequest
import com.example.hotelapp.models.ProfileRequest
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part

interface UserService {
    @PUT("profile/update/client")
    fun updateCredentials(
        @Body request: ChangeCredentialsRequest,
        @Header("Authorization") token: String
    ): Call<User>
    @Multipart
    @PUT("profile/change_avatar")
    fun changeAvatar(
        @Header("Authorization") token: String,
        @Part avatar: MultipartBody.Part
    ): Call<JsonObject>
    @GET("profile/avatar")
    fun getProfileAvatar(@Header("Authorization") token: String): Call<JsonObject>
    @GET("profile/")
    fun getProfileDetails(@Header("Authorization") token: String): Call<JsonObject>
}