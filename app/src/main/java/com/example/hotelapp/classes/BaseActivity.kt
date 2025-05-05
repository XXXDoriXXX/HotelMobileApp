package com.example.hotelapp.classes

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val controller = LanguageController(newBase)
        val localizedContext = controller.getLocalizedContext()
        super.attachBaseContext(localizedContext)
    }
}
