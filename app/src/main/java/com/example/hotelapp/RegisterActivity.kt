package com.example.hotelapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hotelapp.classes.User
import com.example.hotelapp.Holder.UserHolder
import com.example.hotelapp.models.AuthResponse
import com.example.hotelapp.models.RegisterRequest
import com.example.hotelapp.repository.AuthRepository
import com.example.hotelapp.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val usernameField = findViewById<TextInputEditText>(R.id.username_input_field)
        val usersecondnameField = findViewById<TextInputEditText>(R.id.usersecondname_input_field)
        val emailField = findViewById<TextInputEditText>(R.id.email_input_field)
        val phoneField = findViewById<TextInputEditText>(R.id.phone_input_field)
        val passwordField = findViewById<TextInputEditText>(R.id.password_input_field)
        val birthDateField = findViewById<TextInputEditText>(R.id.date_of_birth_input_field)

        val registerButton = findViewById<MaterialButton>(R.id.register_button)
        registerButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val usersecondname = usersecondnameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val phone = phoneField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val birthDate = birthDateField.text.toString().trim()
            val haveaccount: TextView = findViewById(R.id.have_account_text)
            haveaccount.setOnClickListener{
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)

            }
            when {
                username.isEmpty() -> {
                    Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                usersecondname.isEmpty() -> {
                    Toast.makeText(this, "Please enter your last name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                phone.isEmpty() -> {
                    Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                birthDate.isEmpty() -> {
                    Toast.makeText(this, "Please enter your birth date", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val request = RegisterRequest(
                id="1",
                first_name = username,
                last_name = usersecondname,
                email = email,
                phone = phone,
                password = password,
                is_owner = false,
                birth_date = birthDate
            )
            val user =User(username,usersecondname,email,phone,birthDate)
            UserHolder.currentUser = user
            authRepository.registerUser(request).enqueue(object : retrofit2.Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: retrofit2.Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.token
                        if (token != null) {
                            saveToken(token)
                        }
                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        if (token != null) {
                            sessionManager.saveLoginInfo(
                                token = token,
                                firstName = UserHolder.currentUser!!.first_name,
                                lastName = UserHolder.currentUser!!.last_name,
                                email = UserHolder.currentUser!!.email,
                                phone = UserHolder.currentUser!!.phone,
                                birthDate = UserHolder.currentUser!!.birth_date
                            )
                        }
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
    }
    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("access_token", token).apply()
    }
}
