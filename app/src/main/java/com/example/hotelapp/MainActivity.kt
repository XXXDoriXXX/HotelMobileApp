package com.example.hotelapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Правильний виклик super.onCreate
        super.onCreate(savedInstanceState)

        // Застосування теми
        ThemeManager.applyTheme(this)

        // Встановлення макета
        setContentView(R.layout.activity_main)

        // Edge-to-edge дизайн
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Налаштування BottomNavigationView
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNav)
            val navController = findNavController(R.id.fragmentContainerView)
            bottomNavigationView.setupWithNavController(navController)
    }
}
