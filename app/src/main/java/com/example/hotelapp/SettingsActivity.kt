package com.example.hotelapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val systemTheme: RadioButton = findViewById(R.id.radio_system)
        val lightTheme: RadioButton = findViewById(R.id.radio_light)
        val darkTheme: RadioButton = findViewById(R.id.radio_dark)

        val currentTheme = getSharedPreferences("theme_prefs", MODE_PRIVATE)
            .getString("key_theme", ThemeManager.THEME_SYSTEM)

        when (currentTheme) {
            ThemeManager.THEME_LIGHT -> lightTheme.isChecked = true
            ThemeManager.THEME_DARK -> darkTheme.isChecked = true
            ThemeManager.THEME_SYSTEM -> systemTheme.isChecked = true
        }

        systemTheme.setOnClickListener {
            ThemeManager.saveTheme(this, ThemeManager.THEME_SYSTEM)
        }
        lightTheme.setOnClickListener {
            ThemeManager.saveTheme(this, ThemeManager.THEME_LIGHT)
        }
        darkTheme.setOnClickListener {
            ThemeManager.saveTheme(this, ThemeManager.THEME_DARK)
        }
    }
}