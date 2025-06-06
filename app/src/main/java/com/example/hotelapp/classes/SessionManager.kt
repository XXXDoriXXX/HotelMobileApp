package com.example.hotelapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.hotelapp.classes.User


class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "HotelAppSession"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_BIRTH_DATE = "birth_date"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_FAVORITE_HOTEL_IDS = "favorite_hotel_ids"


    }
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    fun saveLoginInfo(
        token: String,
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        birthDate: String
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_ACCESS_TOKEN, token)
            putString(KEY_FIRST_NAME, firstName)
            putString(KEY_LAST_NAME, lastName)
            putString(KEY_EMAIL, email)
            putString(KEY_PHONE, phone)
            putString(KEY_BIRTH_DATE, birthDate)
            apply()
        }
    }
    fun saveUserData(user: User) {
        prefs.edit().apply {
            putString(KEY_FIRST_NAME, user.first_name)
            putString(KEY_LAST_NAME, user.last_name)
            putString(KEY_EMAIL, user.email)
            putString(KEY_PHONE, user.phone)
            putString(KEY_BIRTH_DATE, user.birth_date)
            putString(KEY_AVATAR_URL, user.avatarUrl)
            apply()
        }
    }


    fun getUserBirthDate(): String? {
        return prefs.getString(KEY_BIRTH_DATE, null)
    }

    fun saveUserAvatar(avatarPath: String) {
        prefs.edit().putString("cached_avatar_path", avatarPath).apply()
    }

    fun getUserAvatar(): String? {
        return prefs.getString("cached_avatar_path", null)
    }
    fun getUserFirstName(): String? {
        return prefs.getString(KEY_FIRST_NAME, null)
    }

    fun saveFavoriteHotelIds(ids: List<Int>) {
        val idString = ids.joinToString(",")
        Log.d("FAV_DEBUG", "Зберігаємо ID улюблених: $ids")


        prefs.edit().putString(KEY_FAVORITE_HOTEL_IDS, idString).apply()
    }

    fun getFavoriteHotelIds(): List<Int> {
        val idString = prefs.getString(KEY_FAVORITE_HOTEL_IDS, "") ?: return emptyList()
        return idString.split(",").filter { it.isNotBlank() }.map { it.toInt() }
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

        if (loggedIn) {
            val user = User(
                id = 0,
                first_name = prefs.getString(KEY_FIRST_NAME, "") ?: "",
                last_name = prefs.getString(KEY_LAST_NAME, "") ?: "",
                email = prefs.getString(KEY_EMAIL, "") ?: "",
                birth_date = prefs.getString(KEY_BIRTH_DATE, "") ?: "",
                phone = prefs.getString(KEY_PHONE, "") ?: "",
                avatarUrl = prefs.getString(KEY_AVATAR_URL, "") ?: ""
            )

            UserHolder.currentUser = user
        }

        return loggedIn
    }
    fun clearSession() {
        prefs.edit().clear().apply()
    }

}
