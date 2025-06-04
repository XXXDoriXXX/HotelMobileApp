package com.example.viewmodels
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hotelapp.classes.User
import com.example.hotelapp.repository.UserRepository

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    fun loadProfile(context: Context) {
        _loading.postValue(true)
        repository.loadProfile(
            context = context,
            onSuccess = {
                _user.postValue(it)

            },
            onError = {
                _error.postValue(it.localizedMessage ?: "Unknown error")

            }
        )
    }

}
