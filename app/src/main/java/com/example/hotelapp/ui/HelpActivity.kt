package com.example.hotelapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelapp.R

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        supportActionBar?.apply {
            title = "Help & Support"
            setDisplayHomeAsUpEnabled(true)
        }
        val backBtn = findViewById<ImageView>(R.id.back_button)
        backBtn.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.contact_support_button).setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@hotelapp.com")
                putExtra(Intent.EXTRA_SUBJECT, "Need help")
                putExtra(Intent.EXTRA_TEXT, "Hello, I need help with...")
            }
            startActivity(Intent.createChooser(emailIntent, "Send Email"))
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
