package com.example.hotelapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelapp.R


class SplashActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        UserHolder.initialize(this)

        val logo = findViewById<ImageView>(R.id.splash_logo)
        val sessionManager = UserHolder.getSessionManager()

        intent?.data?.let { deepLinkUri ->
            if (!sessionManager.isLoggedIn()) {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                prefs.edit().putString("pending_deeplink", deepLinkUri.toString()).apply()
            }
        }

        logo.alpha = 0f
        logo.scaleX = 0.8f
        logo.scaleY = 0.8f

        logo.animate()
            .alpha(1f)
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(1500)
            .withEndAction {
                val intent = if (sessionManager.isLoggedIn())
                    Intent(this, MainActivity::class.java)
                else
                    Intent(this, LoginActivity::class.java)

                startActivity(intent)
                finish()
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
