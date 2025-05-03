package com.example.hotelapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        UserHolder.initialize(this)
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_main)

        setupSystemUI()
        setupInsets()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navController = findNavController(R.id.fragmentContainerView)
        bottomNavigationView.setupWithNavController(navController)

        handleStartFragment(intent)
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
