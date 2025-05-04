package com.example.hotelapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hotelapp.R
import com.example.hotelapp.classes.ImageCacheProxy
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var permissionCheckRunnable: Runnable

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("Permissions", "Result: $permissions")

        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val galleryGranted = permissions[Manifest.permission.READ_MEDIA_IMAGES] == true

        if (locationGranted && galleryGranted) {
            handler.removeCallbacks(permissionCheckRunnable)
            updateLocationOnce()
        } else {
            // повтор через 3 сек
            handler.postDelayed(permissionCheckRunnable, 3000)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        UserHolder.initialize(this)
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_main)

        startPermissionCycle()

        setupSystemUI()
        setupInsets()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navController = findNavController(R.id.fragmentContainerView)
        bottomNavigationView.setupWithNavController(navController)

        handleStartFragment(intent)
    }

    private fun startPermissionCycle() {
        permissionCheckRunnable = object : Runnable {
            override fun run() {
                if (hasAllPermissions()) {
                    updateLocationOnce()
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_MEDIA_IMAGES
                        )
                    )
                }
            }
        }

        handler.post(permissionCheckRunnable)
    }
    private fun hasAllPermissions(): Boolean {
        return listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_MEDIA_IMAGES
        ).all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            updateLocationOnce()
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val galleryGranted = permissions[Manifest.permission.READ_MEDIA_IMAGES] == true

        if (locationGranted) updateLocationOnce()
        if (galleryGranted) Log.d("GalleryAccess", "Доступ до фото надано")
    }

    private fun setupSystemUI() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        window.navigationBarColor = Color.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    @SuppressLint("MissingPermission")
    private fun updateLocationOnce() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val city = addresses?.getOrNull(0)?.locality ?: "Unknown City"
                val country = addresses?.getOrNull(0)?.countryName ?: "Unknown Country"
                val fullAddress = "$city, $country"

                getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("last_location", fullAddress)
                    .apply()

                Log.d("GeoUpdate", "Location updated: $fullAddress")
            } else {
                Log.w("GeoUpdate", "Location is null")
            }
        }

    }

    private fun handleStartFragment(intent: Intent?) {
        val startFragment = intent?.getStringExtra("startFragment")
        val openBookingId = intent?.getIntExtra("openBookingId", -1) ?: -1

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        when (startFragment) {
            "home" -> bottomNav.selectedItemId = R.id.homeFragment
            "history" -> {
                bottomNav.selectedItemId = R.id.historyFragment

                if (openBookingId != -1) {
                    navController.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener {
                        override fun onDestinationChanged(
                            controller: NavController,
                            destination: NavDestination,
                            arguments: Bundle?
                        ) {
                            if (destination.id == R.id.historyFragment) {
                                navController.removeOnDestinationChangedListener(this)

                                val fragment = supportFragmentManager
                                    .findFragmentById(R.id.fragmentContainerView) as? HistoryFragment

                                fragment?.viewLifecycleOwnerLiveData?.observe(this@MainActivity) { owner ->
                                    if (owner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                        fragment.showBookingDetailsBottomSheet(openBookingId)
                                    }
                                }
                            }
                        }
                    })
                }
            }
            else -> bottomNav.selectedItemId = R.id.homeFragment
        }

        intent?.removeExtra("startFragment")
        intent?.removeExtra("openBookingId")
    }


}
