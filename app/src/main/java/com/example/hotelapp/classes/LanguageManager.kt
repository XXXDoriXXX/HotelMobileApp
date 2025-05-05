package com.example.hotelapp.classes

import android.content.Context
import android.content.res.Configuration
import java.util.Locale


object LanguageManager {
    fun applyLocale(context: Context, langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}