package com.example.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import HotelItem
import HotelRepository
import com.example.hotelapp.classes.Amenity
import com.example.hotelapp.models.HotelSearchParams

class HotelViewModel(private val hotelRepository: HotelRepository) : ViewModel() {

    private val _hotels = MutableLiveData<List<HotelItem>>()
    val hotels: LiveData<List<HotelItem>> get() = _hotels

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _amenities = MutableLiveData<List<Amenity>>()
    val amenities: LiveData<List<Amenity>> get() = _amenities

    fun loadHotelsByCategory(category: String, city: String, country: String, skip: Int = 0, limit: Int = 25) {
        _loading.value = true
        hotelRepository.getHotelsByCategory(
            category = category,
            city = city,
            country = country,
            skip = skip,
            limit = limit,
            onResult = {
                _hotels.postValue(it)
                _loading.postValue(false)
            },
            onError = {
                _error.postValue(it.localizedMessage ?: "Unknown error")
                _loading.postValue(false)
            }
        )
    }

    fun searchHotelsByFilters(filters: HotelSearchParams) {
        _loading.value = true
        hotelRepository.searchHotelsByFilters(
            filters = filters,
            onResult = {
                _hotels.postValue(it)
                _loading.postValue(false)
            },
            onError = {
                _error.postValue(it.localizedMessage ?: "Search error")
                _loading.postValue(false)
            }
        )
    }

    fun loadHotels() {
        _loading.value = true
        hotelRepository.getHotels(
            onResult = {
                _hotels.postValue(it)
                _loading.postValue(false)
            },
            onError = {
                _error.postValue(it.message)
                _loading.postValue(false)
            }
        )
    }

    fun loadAllAmenities() {
        hotelRepository.getAllAmenities(
            onResult = {
                _amenities.postValue(it)
            },
            onError = {
                _error.postValue(it.localizedMessage ?: "Failed to load amenities")
            }
        )
    }
}
