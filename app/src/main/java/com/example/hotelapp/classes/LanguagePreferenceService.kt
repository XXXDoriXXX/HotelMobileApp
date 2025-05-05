package com.example.hotelapp.classes

import android.content.Context
import android.content.SharedPreferences

class LanguagePreferenceService(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LANGUAGE = "app_language"
        private const val DEFAULT_LANGUAGE = "en"
    }

    fun getCurrentLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun setCurrentLanguage(langCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
    }
}