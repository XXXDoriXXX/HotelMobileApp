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
import com.example.hotelapp.classes.UserHolder
import com.example.hotelapp.models.AuthResponse
import com.example.hotelapp.models.LoginRequest
import com.example.hotelapp.repository.AuthRepository
import com.example.hotelapp.utils.JwtUtils
import com.example.hotelapp.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()
    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        sessionManager = SessionManager(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailOrPhoneField = findViewById<TextInputEditText>(R.id.email_or_phone_input_field)
        val passwordField = findViewById<TextInputEditText>(R.id.password_input_field)
        val haveaccount:TextView = findViewById(R.id.have_account_text)
        haveaccount.setOnClickListener{
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)

        }

        val loginButton = findViewById<MaterialButton>(R.id.login_button)
        loginButton.setOnClickListener {
            val emailOrPhone = emailOrPhoneField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (emailOrPhone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(
                email = emailOrPhone,
                password = password
            )


            authRepository.loginUser(request).enqueue(object : retrofit2.Callback<AuthResponse> {
                override fun onResponse(call: retrofit2.Call<AuthResponse>, response: retrofit2.Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.token
                        if (!token.isNullOrEmpty()) {
                            saveToken(token)
                            UserHolder.currentUser = JwtUtils.parseTokenToUser(token)
                            Toast.makeText(this@LoginActivity, "Welcome, ${UserHolder.currentUser?.first_name}", Toast.LENGTH_SHORT).show()
                            sessionManager.saveLoginInfo(
                                token = token,
                                firstName = UserHolder.currentUser!!.first_name,
                                lastName = UserHolder.currentUser!!.last_name,
                                email = UserHolder.currentUser!!.email,
                                phone = UserHolder.currentUser!!.phone,
                                birthDate = UserHolder.currentUser!!.birth_date
                            )
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Invalid token received", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("access_token", token).apply()
    }
}
