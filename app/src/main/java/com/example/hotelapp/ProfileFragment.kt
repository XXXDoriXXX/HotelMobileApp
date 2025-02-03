package com.example.hotelapp

import UserHolder
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hotelapp.repository.UserRepository
import com.example.hotelapp.utils.SessionManager

class ProfileFragment : Fragment() {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var sessionManager: SessionManager
    private lateinit var avatarImageView: ImageView
    private lateinit var profileEmail: TextView
    private lateinit var profileName: TextView
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val editProfileBtn: Button = view.findViewById(R.id.edit_profile_button)
        val settingsButton: LinearLayout = view.findViewById(R.id.settings_btn)
        val logoutButton: LinearLayout = view.findViewById(R.id.logout_btn)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        avatarImageView = view.findViewById(R.id.profile_avatar)
        profileEmail = view.findViewById(R.id.profile_email)
        profileName = view.findViewById(R.id.profile_name)

        swipeRefreshLayout.setOnRefreshListener {
            refreshProfile()
        }

        editProfileBtn.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        logoutButton.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        avatarImageView.setOnClickListener {
            val cachedImagePath = avatarImageView.tag as? String ?: return@setOnClickListener
            val intent = Intent(requireContext(), FullScreenAvatarActivity::class.java)
            intent.putExtra("CACHED_IMAGE_PATH", cachedImagePath)
            startActivity(intent)
        }

        loadProfile()

        return view
    }

    private fun refreshProfile() {
        swipeRefreshLayout.isRefreshing = true
        loadProfile()
    }

    private fun loadProfile() {
        userRepository.loadProfileAvatar(requireContext(), avatarImageView) { cachedPath ->
            avatarImageView.tag = cachedPath
        }

        userRepository.loadProfileDetails(
            onSuccess = { user ->
                profileName.text = "${user.last_name} ${user.first_name}"
                profileEmail.text = user.email

                swipeRefreshLayout.isRefreshing = false
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Error loading profile: $error", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
        )
    }
}
