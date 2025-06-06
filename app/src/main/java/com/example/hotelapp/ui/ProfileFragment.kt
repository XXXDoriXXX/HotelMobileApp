package com.example.hotelapp.ui

import UserHolder
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.bumptech.glide.Glide
import com.example.hotelapp.R
import com.example.hotelapp.repository.UserRepository
import com.example.hotelapp.utils.SessionManager
import androidx.fragment.app.viewModels
import com.example.hotelapp.classes.SnackBarUtils
import com.example.viewmodels.ProfileViewModel
import com.example.viewmodels.GenericViewModelFactory

class ProfileFragment : Fragment() {
    private val viewModel: ProfileViewModel by viewModels {
        GenericViewModelFactory(ProfileViewModel::class.java) {
            ProfileViewModel(UserRepository())
        }
    }
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var sessionManager: SessionManager
    private lateinit var avatarImageView: ImageView
    private lateinit var profileEmail: TextView
    private lateinit var profileName: TextView
    private lateinit var rootView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sessionManager = SessionManager(requireContext())
        rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                profileName.text = "${user.last_name} ${user.first_name}"
                profileEmail.text = user.email

                avatarImageView.tag = user.avatarUrl
                Glide.with(requireContext())
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.default_avatar)
                    .into(avatarImageView)
                swipeRefreshLayout.isRefreshing = false

            }
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            swipeRefreshLayout.isRefreshing = it
        }

        viewModel.error.observe(viewLifecycleOwner) {
            SnackBarUtils.showLong(requireContext(),rootView, R.string.toast_error_with_reason, "Error loading profile: $it")
            swipeRefreshLayout.isRefreshing = false

        }
        viewModel.loadProfile(requireContext())
        val editProfileBtn: Button = rootView.findViewById(R.id.edit_profile_button)
        val settingsButton: LinearLayout = rootView.findViewById(R.id.settings_btn)
        val logoutButton: LinearLayout = rootView.findViewById(R.id.logout_btn)
        val support_btn: LinearLayout=rootView.findViewById(R.id.help_page_btn)
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)
        avatarImageView = rootView.findViewById(R.id.profile_avatar)
        profileEmail = rootView.findViewById(R.id.profile_email)
        profileName = rootView.findViewById(R.id.profile_name)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadProfile(requireContext())
        }

        editProfileBtn.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        support_btn.setOnClickListener {
            val intent = Intent(requireContext(), HelpActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
        if (::avatarImageView.isInitialized) {
        avatarImageView.setOnClickListener {
            val currentImagePath = avatarImageView.tag as? String
            if (!currentImagePath.isNullOrEmpty()) {
                val intent = Intent(requireContext(), FullScreenAvatarActivity::class.java)
                intent.putExtra("CACHED_IMAGE_PATH", currentImagePath)
                startActivity(intent)
            }
        }
        }

        return rootView
    }



}
