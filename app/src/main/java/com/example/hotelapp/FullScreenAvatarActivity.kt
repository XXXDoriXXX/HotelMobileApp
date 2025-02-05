package com.example.hotelapp

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.hotelapp.utils.SessionManager

class FullScreenAvatarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val sessionManager = SessionManager(this)
        val cachedImagePath = sessionManager.getUserAvatar()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_full_screen_avatar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val avatarImageView: ImageView = findViewById(R.id.fullscreen_avatar)
        val closeButton: View = findViewById(R.id.close_button)
        if (!cachedImagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(cachedImagePath)
                .apply(
                    RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.default_avatar)
                )
                .into(avatarImageView)
        }

        closeButton.setOnClickListener { finish() }
        avatarImageView.setOnClickListener { finish() }
    }
}
