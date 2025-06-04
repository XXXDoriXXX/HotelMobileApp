package com.example.hotelapp.classes

import android.content.Context

class LanguageController(private val context: Context) {
    private val preferenceService = LanguagePreferenceService(context)

    fun switchLanguage(langCode: String) {
        preferenceService.setCurrentLanguage(langCode)
    }

    fun getLocalizedContext(): Context {
        val langCode = preferenceService.getCurrentLanguage()
        return LanguageManager.applyLocale(context, langCode)
    }
}
