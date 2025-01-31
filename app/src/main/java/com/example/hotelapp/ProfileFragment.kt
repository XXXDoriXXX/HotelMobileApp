package com.example.hotelapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.hotelapp.Holder.apiHolder
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.classes.ImageCacheProxy
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.utils.SessionManager
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var sessionManager: SessionManager
    private lateinit var avatarImageView: ImageView
    private val PICK_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        sessionManager = SessionManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ImageCacheProxy.initialize(requireContext())
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val editprofilebtn:Button = view.findViewById(R.id.edit_profile_button)
        val settingsButton: LinearLayout = view.findViewById(R.id.settings_btn)
        settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }
        editprofilebtn.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent) }
        val logoutButton: LinearLayout = view.findViewById(R.id.logout_btn)
        logoutButton.setOnClickListener {
            sessionManager.clearSession()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        val profileEmail: TextView = view.findViewById(R.id.profile_email)
        val profileName: TextView = view.findViewById(R.id.profile_name)
        profileName.text = "${UserHolder.currentUser?.last_name} ${UserHolder.currentUser?.first_name}"
        profileEmail.text = UserHolder.currentUser?.email
        avatarImageView = view.findViewById(R.id.profile_avatar)

        loadProfileAvatar()

        avatarImageView.setOnClickListener {
            openGallery()
        }

        return view
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

                        ImageCacheProxy.clearCachedImage(fullAvatarUrl)

                        val newAvatarUrl = "$fullAvatarUrl?timestamp=${System.currentTimeMillis()}"

                        Glide.with(requireContext())
                            .load(newAvatarUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(R.drawable.default_avatar)
                            .into(avatarImageView)
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("ProfileFragment", "Error loading avatar: ${t.message}")
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
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
                    Log.d("ProfileFragment", "Avatar updated: $avatarUrl")

                    if (!avatarUrl.isNullOrEmpty()) {
                        val fullAvatarUrl = "${apiHolder.BASE_URL}$avatarUrl"

                        ImageCacheProxy.clearCachedImage(fullAvatarUrl)

                        val newAvatarUrl = "$fullAvatarUrl?timestamp=${System.currentTimeMillis()}"

                        Glide.with(requireContext())
                            .load(newAvatarUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(R.drawable.default_avatar)
                            .into(avatarImageView)

                        loadProfileAvatar()
                    }
                } else {
                    Log.e("ProfileFragment", "Failed to update avatar: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("ProfileFragment", "Avatar upload failed: ${t.message}")
                t.printStackTrace()
            }
        })
    }


    private fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
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
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
