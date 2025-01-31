package com.example.hotelapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.hotelapp.Holder.apiHolder
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.classes.ImageCacheProxy
import com.example.hotelapp.classes.User
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.repository.UserRepository
import com.example.hotelapp.utils.SessionManager
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class EditProfileActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var avatarImageView: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        UserHolder.initialize(this)
        userRepository = UserRepository()
        sessionManager = UserHolder.getSessionManager()

        val firstNameField = findViewById<EditText>(R.id.edit_first_name)
        val lastNameField = findViewById<EditText>(R.id.edit_last_name)
        val emailField = findViewById<EditText>(R.id.edit_email)
        val phoneField = findViewById<EditText>(R.id.edit_phone)
        val birthDateField = findViewById<EditText>(R.id.edit_birth_date)
        val saveButton = findViewById<Button>(R.id.save_btn)
        avatarImageView = findViewById(R.id.profile_avatar)

        val currentUser = UserHolder.currentUser
        currentUser?.let {
            firstNameField.setText(it.first_name)
            lastNameField.setText(it.last_name)
            emailField.setText(it.email)
            phoneField.setText(it.phone)
            birthDateField.setText(it.birth_date)
        }

        loadProfileAvatar()

        avatarImageView.setOnClickListener {
            openGallery()
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

    private fun loadProfileAvatar() {
        val token = sessionManager.getAccessToken() ?: return
        val apiService = RetrofitClient.retrofit.create(HotelService::class.java)

        apiService.getProfileAvatar("Bearer $token").enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val avatarUrl = response.body()?.get("avatar_url")?.asString
                    if (!avatarUrl.isNullOrEmpty()) {
                        val fullAvatarUrl = "${apiHolder.BASE_URL}$avatarUrl"

                        // 🛑 Видаляємо кеш, щоб оновити аватар
                        ImageCacheProxy.clearCachedImage(fullAvatarUrl)

                        val newAvatarUrl = "$fullAvatarUrl?timestamp=${System.currentTimeMillis()}"

                        Glide.with(this@EditProfileActivity)
                            .load(newAvatarUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(R.drawable.default_avatar)
                            .into(avatarImageView)
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("EditProfileActivity", "Error loading avatar: ${t.message}")
            }
        })
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
        val token = sessionManager.getAccessToken() ?: return
        val filePath = getPathFromUri(uri) ?: return
        val file = File(filePath)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val apiService = RetrofitClient.retrofit.create(HotelService::class.java)
        apiService.changeAvatar("Bearer $token", body).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val avatarUrl = response.body()?.get("avatar_url")?.asString
                    Log.d("EditProfileActivity", "Avatar updated: $avatarUrl")

                    if (!avatarUrl.isNullOrEmpty()) {
                        val fullAvatarUrl = "${apiHolder.BASE_URL}$avatarUrl"

                        // 🛑 Видаляємо кеш, щоб оновити аватар
                        ImageCacheProxy.clearCachedImage(fullAvatarUrl)

                        val newAvatarUrl = "$fullAvatarUrl?timestamp=${System.currentTimeMillis()}"

                        Glide.with(this@EditProfileActivity)
                            .load(newAvatarUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(R.drawable.default_avatar)
                            .into(avatarImageView)

                        loadProfileAvatar()
                    }
                } else {
                    Log.e("EditProfileActivity", "Failed to update avatar: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("EditProfileActivity", "Avatar upload failed: ${t.message}")
                t.printStackTrace()
            }
        })
    }

    private fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val filePath = cursor?.getString(columnIndex ?: 0)
        cursor?.close()
        return filePath
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
}
