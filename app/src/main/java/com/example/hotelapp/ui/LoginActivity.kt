package com.example.hotelapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hotelapp.R
import com.example.hotelapp.models.AuthResponse
import com.example.hotelapp.models.LoginRequest
import com.example.hotelapp.repository.AuthRepository
import com.example.hotelapp.utils.JwtUtils
import com.example.hotelapp.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        sessionManager = SessionManager(this)
        val logo = findViewById<ImageView>(R.id.register_logo)
        val title = findViewById<LinearLayout>(R.id.register_title)

        logo.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(200)
            .start()

        title.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(400)
            .start()

        val root = findViewById<View>(R.id.login_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailField = findViewById<TextInputEditText>(R.id.email_or_phone_input_field)
        val passwordField = findViewById<TextInputEditText>(R.id.password_input_field)
        val emailLayout = findViewById<TextInputLayout>(R.id.email_input)
        val passwordLayout = findViewById<TextInputLayout>(R.id.password_input)
        val haveAccount = findViewById<TextView>(R.id.have_account_text)
        val loginButton = findViewById<MaterialButton>(R.id.login_button)

        haveAccount.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            emailLayout.error = null
            passwordLayout.error = null

            if (email.isEmpty()) {
                emailLayout.error = "Поле не може бути порожнім"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = "Це не схоже на email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordLayout.error = "Введіть пароль"
                return@setOnClickListener
            }
            if (password.length < 6) {
                passwordLayout.error = "Пароль має бути хоча б 6 символів"
                return@setOnClickListener
            }

            loginButton.isEnabled = false

            val request = LoginRequest(email = email, password = password)
            authRepository.loginUser(request).enqueue(object : retrofit2.Callback<AuthResponse> {
                override fun onResponse(
                    call: retrofit2.Call<AuthResponse>,
                    response: retrofit2.Response<AuthResponse>
                ) {
                    loginButton.isEnabled = true
                    if (response.isSuccessful) {
                        val token = response.body()?.token
                        if (!token.isNullOrEmpty()) {
                            saveToken(token)
                            UserHolder.currentUser = JwtUtils.parseTokenToUser(token)
                            Toast.makeText(
                                this@LoginActivity,
                                "Вітаю, ${UserHolder.currentUser?.first_name}",
                                Toast.LENGTH_SHORT
                            ).show()
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
                            Toast.makeText(
                                this@LoginActivity,
                                "Сервер повернув некоректний токен",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        passwordLayout.error = "Невірний логін або пароль"

                    }
                }

                override fun onFailure(call: retrofit2.Call<AuthResponse>, t: Throwable) {
                    loginButton.isEnabled = true
                    Toast.makeText(
                        this@LoginActivity,
                        "Помилка: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("access_token", token).apply()
    }
}
