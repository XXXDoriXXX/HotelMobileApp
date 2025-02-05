package com.example.hotelapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelapp.Holder.apiHolder


class SplashActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserHolder.initialize(this)
        val sessionManager=UserHolder.getSessionManager();
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.isLoggedIn()) {
                navigateToMainActivity()
            } else {
                navigateToLogin()
            }
        }, 2000)
        if (sessionManager.isLoggedIn()) {
            navigateToMainActivity()
        } else {
            navigateToLogin()
        }

    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
