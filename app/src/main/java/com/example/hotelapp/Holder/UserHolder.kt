package com.example.hotelapp.Holder

import HotelRepository
import android.content.Context
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.classes.User
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.utils.SessionManager

object UserHolder {
    var currentUser: User? = null

    private val apiService = RetrofitClient.retrofit.create(HotelService::class.java)
    lateinit var sessionManager: SessionManager
        private set
    private var hotelRepositoryInstance: HotelRepository? = null

    fun initialize(context: Context) {
        sessionManager = SessionManager(context)
    }

    fun getHotelRepository(): HotelRepository {
        if (!::sessionManager.isInitialized) {
            throw IllegalStateException("SessionManager must be initialized before accessing HotelRepository")
        }

        if (hotelRepositoryInstance == null) {
            hotelRepositoryInstance = HotelRepository(apiService, sessionManager)
        }

        return hotelRepositoryInstance!!
    }
}
