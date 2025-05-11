package com.example.hotelapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hotelapp.R
import com.example.hotelapp.classes.SnackBarUtils
import com.example.hotelapp.classes.User
import com.example.hotelapp.models.AuthResponse
import com.example.hotelapp.models.RegisterRequest
import com.example.hotelapp.repository.AuthRepository
import com.example.hotelapp.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import com.hbb20.CountryCodePicker
import retrofit2.Call

class RegisterActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sessionManager = SessionManager(this)
        setContentView(R.layout.activity_register)

        val rootView = findViewById<android.view.View>(R.id.register_root)
        ThemeManager.applyTheme(this)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logo = findViewById<ImageView>(R.id.register_logo)
        val title = findViewById<LinearLayout>(R.id.register_title)
        val datePicker = findViewById<DatePicker>(R.id.edit_birth_date_picker)
        logo.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(200).start()
        title.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(400).start()

        val ccp = findViewById<CountryCodePicker>(R.id.ccp)
        val phoneField = findViewById<TextInputEditText>(R.id.phone_input_field)
        ccp.registerCarrierNumberEditText(phoneField)


        val usernameField = findViewById<TextInputEditText>(R.id.username_input_field)
        val usersecondnameField = findViewById<TextInputEditText>(R.id.usersecondname_input_field)
        val emailField = findViewById<TextInputEditText>(R.id.email_input_field)
        val passwordField = findViewById<TextInputEditText>(R.id.password_input_field)

        usernameField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        usersecondnameField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        emailField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        phoneField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        passwordField.setImeOptions(EditorInfo.IME_ACTION_NEXT)

        val firstNameLayout = findViewById<TextInputLayout>(R.id.username_input)
        val lastNameLayout = findViewById<TextInputLayout>(R.id.usersecondname_input)
        val emailLayout = findViewById<TextInputLayout>(R.id.email_input)
        val passwordLayout = findViewById<TextInputLayout>(R.id.password_input)

        val haveAccount: LinearLayout = findViewById(R.id.have_account_text)
        haveAccount.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
        }

        val registerButton = findViewById<MaterialButton>(R.id.register_button)
        registerButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val usersecondname = usersecondnameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val phone = ccp.fullNumberWithPlus.trim()
            val password = passwordField.text.toString().trim()

            val year = datePicker.year
            val month = datePicker.month
            val day = datePicker.dayOfMonth
            val birthDate = "%04d-%02d-%02d".format(year, month + 1, day)

            firstNameLayout.error = null
            lastNameLayout.error = null
            emailLayout.error = null
            passwordLayout.error = null

            val today = java.util.Calendar.getInstance()
            val dob = java.util.Calendar.getInstance()
            dob.set(year, month, day)

            var age = today.get(java.util.Calendar.YEAR) - dob.get(java.util.Calendar.YEAR)
            if (today.get(java.util.Calendar.DAY_OF_YEAR) < dob.get(java.util.Calendar.DAY_OF_YEAR)) {
                age--
            }

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
                phone.isEmpty() || !phone.matches(Regex("^\\+\\d{10,15}$")) -> {
                    SnackBarUtils.showLong(this, rootView, R.string.valid_phone_number)
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    passwordLayout.error = "Password must be at least 6 characters"
                    return@setOnClickListener
                }
                age < 18 -> {
                    SnackBarUtils.showLong(this, rootView, R.string.valid_ears_old)
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
                        SnackBarUtils.showLong(this@RegisterActivity, rootView, R.string.register_succesful)
                        sessionManager.saveLoginInfo(token ?: "", username, usersecondname, email, phone, birthDate)
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                        SnackBarUtils.showLong(this@RegisterActivity, rootView, R.string.network_error, errorMessage)
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    SnackBarUtils.showLong(this@RegisterActivity, rootView, R.string.network_error, t.message ?: "")
                }
            })
        }
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("access_token", token).apply()
    }
}
