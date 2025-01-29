package com.example.hotelapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hotelapp.classes.User
import com.example.hotelapp.repository.UserRepository
import com.example.hotelapp.utils.SessionManager

class EditProfileActivity : AppCompatActivity() {
    private lateinit var userRepository: UserRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserHolder.initialize(this);
        userRepository = UserRepository()
        val sessionManager = UserHolder.getSessionManager()
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val firstNameField = findViewById<EditText>(R.id.edit_first_name)
        val lastNameField = findViewById<EditText>(R.id.edit_last_name)
        val emailField = findViewById<EditText>(R.id.edit_email)
        val phoneField = findViewById<EditText>(R.id.edit_phone)
        val birthDateField = findViewById<EditText>(R.id.edit_birth_date)
        val saveButton = findViewById<Button>(R.id.save_btn)

        val currentUser = UserHolder.currentUser
        currentUser?.let {
            firstNameField.setText(it.first_name)
            lastNameField.setText(it.last_name)
            emailField.setText(it.email)
            phoneField.setText(it.phone)
            birthDateField.setText(it.birth_date)
        }

        saveButton.setOnClickListener {
            val updatedUser = User(
                first_name = firstNameField.text.toString().trim(),
                last_name = lastNameField.text.toString().trim(),
                email = emailField.text.toString().trim(),
                phone = phoneField.text.toString().trim(),
                birth_date = birthDateField.text.toString().trim()
            )

            userRepository.updateUserProfile(updatedUser,
                onSuccess = {
                    UserHolder.currentUser = updatedUser
                    sessionManager.saveUserData(updatedUser)
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = { error ->
                    Toast.makeText(this, "Update failed: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}