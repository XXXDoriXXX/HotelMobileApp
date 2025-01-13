package com.example.hotelapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.hotelapp.classes.User
import com.example.hotelapp.classes.UserHolder

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
    }

    // Збереження даних при логіні
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

    // Отримання доступного токена
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    // Перевірка, чи користувач залогінений
    fun isLoggedIn(): Boolean {
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

        if (loggedIn) {
            val user = User(
                first_name = prefs.getString(KEY_FIRST_NAME, "") ?: "",
                last_name = prefs.getString(KEY_LAST_NAME, "") ?: "",
                email = prefs.getString(KEY_EMAIL, "") ?: "",
                phone = prefs.getString(KEY_PHONE, "") ?: "",
                birth_date = prefs.getString(KEY_BIRTH_DATE, "") ?: ""
            )
            UserHolder.currentUser = user
        }

        return loggedIn
    }


    // Отримання збережених даних користувача


    // Очищення сесії
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
