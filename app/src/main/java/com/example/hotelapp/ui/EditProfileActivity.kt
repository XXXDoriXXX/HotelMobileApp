package com.example.hotelapp.ui

import UserHolder
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hotelapp.R
import com.example.hotelapp.models.ChangeCredentialsRequest
import com.example.hotelapp.repository.UserRepository
import com.example.hotelapp.utils.SessionManager

class EditProfileActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var avatarImageView: ImageView
    private lateinit var firstNameField: EditText
    private lateinit var lastNameField: EditText
    private lateinit var emailField: EditText
    private lateinit var phoneField: EditText
    private lateinit var birthDateField: EditText
    private lateinit var userRepository: UserRepository
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        userRepository = UserRepository()

        firstNameField = findViewById(R.id.edit_first_name)
        lastNameField = findViewById(R.id.edit_last_name)
        emailField = findViewById(R.id.edit_email)
        phoneField = findViewById(R.id.edit_phone)
        birthDateField = findViewById(R.id.edit_birth_date)
        avatarImageView = findViewById(R.id.profile_avatar)
        val saveButton: Button = findViewById(R.id.save_btn)

        loadProfile()

        avatarImageView.setOnClickListener {
            openGallery()
        }

        saveButton.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadProfile() {
        userRepository.loadProfileDetails(
            context = this,
            avatarImageView = avatarImageView,
            onSuccess = { user ->
                firstNameField.setText(user.first_name)
                lastNameField.setText(user.last_name)
                emailField.setText(user.email)
                phoneField.setText(user.phone)
                birthDateField.setText(user.birth_date)
                avatarImageView.tag = user.avatarUrl
            },
            onError = { error ->
                Toast.makeText(this, "Error loading profile: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }


    private fun updateProfile() {
        val user = UserHolder.currentUser ?: return
        val userId = user.id
        val userAvatarUrl = user.avatarUrl

        val currentPassword = findViewById<EditText>(R.id.edit_old_password).text.toString().trim()
        val newPassword = findViewById<EditText>(R.id.edit_new_password).text.toString().trim()
        val confirmPassword = findViewById<EditText>(R.id.edit_confirm_password).text.toString().trim()

        val changeRequest = ChangeCredentialsRequest(
            current_password = currentPassword.ifEmpty { " " },
            confirm_password = confirmPassword.ifEmpty { " " },
            new_password = if (newPassword.isNotEmpty()) newPassword else null,
            new_email = if (emailField.text.toString().trim() != user.email) emailField.text.toString().trim() else null,
            first_name = firstNameField.text.toString().trim(),
            last_name = lastNameField.text.toString().trim(),
            phone = phoneField.text.toString().trim(),
            birth_date = birthDateField.text.toString().trim()
        )


        userRepository.updateCredentials(
            context = this,
            request = changeRequest,
            onSuccess = { updatedUser ->
                sessionManager.saveUserData(updatedUser)
                Toast.makeText(this, "Дані оновлено успішно!", Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { error ->
                Toast.makeText(this, "Помилка: $error", Toast.LENGTH_LONG).show()
            }
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                uploadNewAvatar(uri)
            }
        }
    }
    private fun uploadNewAvatar(uri: Uri) {
        userRepository.uploadNewAvatar(
            context = this,
            uri = uri,
            avatarImageView = avatarImageView,
            onSuccess = {
                loadProfile()
            },
            onError = { error ->
                Toast.makeText(this, "Failed to update avatar: $error", Toast.LENGTH_LONG).show()
            }
        )
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
}
