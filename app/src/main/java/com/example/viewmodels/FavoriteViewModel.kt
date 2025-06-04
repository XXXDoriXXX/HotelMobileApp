package com.example.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import HotelItem
import HotelRepository


class FavoriteViewModel(private val repository: HotelRepository) : ViewModel() {

    private val _favorites = MutableLiveData<List<HotelItem>>()
    val favorites: LiveData<List<HotelItem>> get() = _favorites

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadFavorites() {
        repository.getFavorites(
            onResult = { _favorites.postValue(it) },
            onError = { _error.postValue(it.message) }
        )
    }
}
