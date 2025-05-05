package com.example.hotelapp.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hotelapp.R
import com.example.hotelapp.classes.User
import com.example.hotelapp.models.AuthResponse
import com.example.hotelapp.models.RegisterRequest
import com.example.hotelapp.repository.AuthRepository
import com.example.hotelapp.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import retrofit2.Call

class RegisterActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sessionManager = SessionManager(this)
        setContentView(R.layout.activity_register)
        ThemeManager.applyTheme(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logo = findViewById<ImageView>(R.id.register_logo)
        val title = findViewById<LinearLayout>(R.id.register_title)

        logo.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(200).start()
        title.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(400).start()

        val usernameField = findViewById<TextInputEditText>(R.id.username_input_field)
        val usersecondnameField = findViewById<TextInputEditText>(R.id.usersecondname_input_field)
        val emailField = findViewById<TextInputEditText>(R.id.email_input_field)
        val phoneField = findViewById<TextInputEditText>(R.id.phone_input_field)
        val passwordField = findViewById<TextInputEditText>(R.id.password_input_field)
        val birthDateField = findViewById<TextInputEditText>(R.id.date_of_birth_input_field)

        usernameField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        usersecondnameField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        emailField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        phoneField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        passwordField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        birthDateField.setImeOptions(EditorInfo.IME_ACTION_DONE)

        val firstNameLayout = findViewById<TextInputLayout>(R.id.username_input)
        val lastNameLayout = findViewById<TextInputLayout>(R.id.usersecondname_input)
        val emailLayout = findViewById<TextInputLayout>(R.id.email_input)
        val phoneLayout = findViewById<TextInputLayout>(R.id.phone_input)
        val passwordLayout = findViewById<TextInputLayout>(R.id.password_input)
        val birthLayout = findViewById<TextInputLayout>(R.id.date_of_birth_input)

        val haveaccount: LinearLayout = findViewById(R.id.have_account_text)
        haveaccount.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
        }

        val registerButton = findViewById<MaterialButton>(R.id.register_button)
        registerButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val usersecondname = usersecondnameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val phone = phoneField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val birthDate = birthDateField.text.toString().trim().replace("/", "-")

            // Clear previous errors
            firstNameLayout.error = null
            lastNameLayout.error = null
            emailLayout.error = null
            phoneLayout.error = null
            passwordLayout.error = null
            birthLayout.error = null

            when {
                username.isEmpty() -> {
                    firstNameLayout.error = "First name required"
                    return@setOnClickListener
                }
                usersecondname.isEmpty() -> {
                    lastNameLayout.error = "Last name required"
                    return@setOnClickListener
                }
                email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailLayout.error = "Enter a valid email"
                    return@setOnClickListener
                }
                phone.isEmpty() -> {
                    phoneLayout.error = "Phone number required"
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    passwordLayout.error = "Password must be at least 6 characters"
                    return@setOnClickListener
                }
                !birthDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
                    birthLayout.error = "Date format must be YYYY-MM-DD"
                    return@setOnClickListener
                }
            }

            val request = RegisterRequest(username, usersecondname, email, phone, password, birthDate)
            val user = User(0, username, usersecondname, email, phone, birthDate, "")

            UserHolder.currentUser = user
            authRepository.registerUser(request).enqueue(object : retrofit2.Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: retrofit2.Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.token
                        if (token != null) saveToken(token)
                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        sessionManager.saveLoginInfo(token ?: "", username, usersecondname, email, phone, birthDate)
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                        Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        birthDateField.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                val input = s.toString().replace("/", "")
                val formatted = StringBuilder()
                for (i in input.indices) {
                    formatted.append(input[i])
                    if ((i == 3 || i == 5) && i != input.length - 1) formatted.append("/")
                }
                birthDateField.setText(formatted.toString())
                birthDateField.setSelection(formatted.length)
                isEditing = false
            }
        })
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("access_token", token).apply()
    }
}