package com.example.hotelapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hotelapp.Holder.apiHolder
import com.example.hotelapp.classes.ImageCacheProxy
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        UserHolder.initialize(this)
        ImageCacheProxy.initialize(this)
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)

        setContentView(R.layout.activity_main)

        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        window.navigationBarColor = Color.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(R.id.fragmentContainerView)
        bottomNavigationView.setupWithNavController(navController)
        requestLocationPermissionsAndFetch()
    }

    private val locationPermissionsRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fine || coarse) {
                fetchAndSaveLocation()
            } else {
                Log.w("MainActivity", "Location permission denied")
            }
        }

    private fun requestLocationPermissionsAndFetch() {
        locationPermissionsRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchAndSaveLocation() {
        val fused = LocationServices.getFusedLocationProviderClient(this)

        fused.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.ENGLISH)
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val addressText = addresses?.firstOrNull()?.let {
                    "${it.locality ?: "Unknown City"}, ${it.countryName ?: "Unknown Country"}"
                } ?: "Unknown Location"

                Log.d("MainActivity", "Fetched location: $addressText")

                getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("last_location", addressText)
                    .apply()
            }
        }.addOnFailureListener {
            Log.e("MainActivity", "Failed to fetch location: ${it.message}")
        }
    }
}
