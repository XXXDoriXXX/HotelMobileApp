package com.example.hotelapp.ui

import UserHolder
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hotelapp.R
import com.example.hotelapp.classes.SnackBarUtils
import com.example.hotelapp.models.ChangeCredentialsRequest
import com.example.hotelapp.repository.UserRepository
import com.example.hotelapp.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

class EditProfileActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var avatarImageView: ImageView
    private lateinit var firstNameField: EditText
    private lateinit var lastNameField: EditText
    private lateinit var emailField: EditText
    private lateinit var phoneField: EditText
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

        firstNameField = findViewById(R.id.username_input_field)
        lastNameField = findViewById(R.id.usersecondname_input_field)
        emailField = findViewById(R.id.email_input_field)
        phoneField = findViewById(R.id.phone_input_field)
        avatarImageView = findViewById(R.id.profile_avatar)
        val saveButton: Button = findViewById(R.id.save_btn)
        val back_btn:ImageView = findViewById(R.id.back_button)
        loadProfile()

        back_btn.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.dialog_title_exit))
                .setMessage(getString(R.string.dialog_message_exit))
                .setPositiveButton(getString(R.string.dialog_positive)){ _, _ ->
                    updateProfile()
                }
                .setNegativeButton(getString(R.string.dialog_negative)) { _, _ ->
                    onBackPressedDispatcher.onBackPressed()
                }
                .setNeutralButton(getString(R.string.dialog_neutral), null)
                .show()
        }



        val datePicker = findViewById<DatePicker>(R.id.edit_birth_date_picker)
        val birthDate = sessionManager.getUserBirthDate()

        if (!birthDate.isNullOrBlank()) {
            val parts = birthDate.split("-")
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull() ?: 2000
                val month = parts[1].toIntOrNull()?.minus(1) ?: 0
                val day = parts[2].toIntOrNull() ?: 1
                datePicker.updateDate(year, month, day)
            }
        }



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

                val cleanedDate = user.birth_date.substringBefore("T")
                val parts = cleanedDate.split("-")
                if (parts.size == 3) {
                    val year = parts[0].toIntOrNull() ?: 2000
                    val month = parts[1].toIntOrNull()?.minus(1) ?: 0
                    val day = parts[2].toIntOrNull() ?: 1
                    findViewById<DatePicker>(R.id.edit_birth_date_picker).updateDate(year, month, day)
                }

                avatarImageView.tag = user.avatarUrl
            },
            onError = { error ->
                SnackBarUtils.showLong(
                    context = this,
                    view = findViewById(R.id.main),
                    stringRes = R.string.toast_profile_error,
                    error
                )
            }
        )
    }
    private fun removeNumberPickerBackgrounds() {
        val datePicker = findViewById<DatePicker>(R.id.edit_birth_date_picker)

        listOf("day", "month", "year").forEach { field ->
            val id = resources.getIdentifier(field, "id", "android")
            val picker = datePicker.findViewById<NumberPicker>(id)
            picker?.let {
                it.setBackgroundColor(Color.TRANSPARENT)
                for (i in 0 until it.childCount) {
                    it.getChildAt(i)?.setBackgroundColor(Color.TRANSPARENT)
                }

                try {
                    val selectionDividerField = NumberPicker::class.java.getDeclaredField("SelectionDivider")
                    selectionDividerField.isAccessible = true
                    selectionDividerField.set(it, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    private fun updateProfile() {
        val user = UserHolder.currentUser ?: return

        val firstNameLayout = findViewById<TextInputLayout>(R.id.username_input)
        val lastNameLayout = findViewById<TextInputLayout>(R.id.usersecondname_input)
        val emailLayout = findViewById<TextInputLayout>(R.id.email_input)
        val phoneLayout = findViewById<TextInputLayout>(R.id.phone_input)

        val firstName = findViewById<TextInputEditText>(R.id.username_input_field).text.toString().trim()
        val lastName = findViewById<TextInputEditText>(R.id.usersecondname_input_field).text.toString().trim()
        val email = findViewById<TextInputEditText>(R.id.email_input_field).text.toString().trim()
        val phone = findViewById<TextInputEditText>(R.id.phone_input_field).text.toString().trim()

        val currentPassword = findViewById<TextInputEditText>(R.id.edit_old_password_field).text.toString().trim()
        val newPassword = findViewById<TextInputEditText>(R.id.edit_new_password_field).text.toString().trim()
        val confirmPassword = findViewById<TextInputEditText>(R.id.edit_confirm_password_field).text.toString().trim()

        val datePicker = findViewById<DatePicker>(R.id.edit_birth_date_picker)
        val birthDate = "%04d-%02d-%02d".format(
            datePicker.year,
            datePicker.month + 1,
            datePicker.dayOfMonth
        )

        var hasError = false

        // Валідація
        if (firstName.isEmpty()) {
            firstNameLayout.error = getString(R.string.error_enter_first_name)
            hasError = true
        } else firstNameLayout.error = null

        if (lastName.isEmpty()) {
            lastNameLayout.error = getString(R.string.error_enter_last_name)
            hasError = true
        } else lastNameLayout.error = null

        if (phone.isEmpty()) {
            phoneLayout.error = getString(R.string.error_enter_phone)
            hasError = true
        } else phoneLayout.error = null

        if (email.isEmpty()) {
            emailLayout.error = getString(R.string.error_enter_email)
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.error_invalid_email)
            hasError = true
        } else emailLayout.error = null

        if (currentPassword.isBlank()) {
            findViewById<TextInputLayout>(R.id.edit_old_password).error = getString(R.string.error_required)
            hasError = true
        } else {
            findViewById<TextInputLayout>(R.id.edit_old_password).error = null
        }

        if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
            findViewById<TextInputLayout>(R.id.edit_old_password).error = getString(R.string.error_password_mismatch)
            hasError = true
        } else {
            findViewById<TextInputLayout>(R.id.edit_confirm_password).error = null
        }

        if (hasError) return

        val changeRequest = ChangeCredentialsRequest(
            current_password = currentPassword,
            confirm_password = confirmPassword.ifEmpty { " " },
            new_password = if (newPassword.isNotEmpty()) newPassword else null,
            new_email = if (email != user.email) email else null,
            first_name = firstName,
            last_name = lastName,
            phone = phone,
            birth_date = birthDate
        )

        userRepository.updateCredentials(
            context = this,
            request = changeRequest,
            onSuccess = { updatedUser ->
                sessionManager.saveUserData(updatedUser)
                finish()
            },
            onError = { error ->
                emailLayout.error = error
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
                SnackBarUtils.showShort(
                    context = this,
                    view = findViewById(R.id.main),
                    stringRes = R.string.toast_avatar_error,
                    error
                )
            }
        )
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
}
