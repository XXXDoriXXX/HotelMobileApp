package com.example.hotelapp

import HotelItem
import HotelRepository
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.Holder.UserHolder
import com.example.hotelapp.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var searchInputField: TextInputEditText
    private lateinit var itemsList: RecyclerView
    private val hotelRepository = UserHolder.getHotelRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            searchInputField = view.findViewById(R.id.search_input_field)
            itemsList = view.findViewById(R.id.itemsHotelList)

            itemsList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            loadDefaultHotels()
            val debounceSearch = debounce<String>(
                delayMillis = 500L,
                coroutineScope = viewLifecycleOwner.lifecycleScope
            ) { query ->
                performSearch(query)
            }

            searchInputField.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    debounceSearch(s.toString())
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error initializing views: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun loadDefaultHotels() {
        hotelRepository.getHotels(
            onResult = { hotels ->
                if (isAdded) {
                    itemsList.adapter = ItemsHotelAdapter(hotels, requireContext())
                }
            },
            onError = { error ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            loadDefaultHotels()
            return
        }

        hotelRepository.searchHotels(query,
            onResult = { hotels ->
                if (isAdded) {
                    itemsList.adapter = ItemsHotelAdapter(hotels, requireContext())
                }
            },
            onError = { error ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Search: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun <T> debounce(
        delayMillis: Long = 500L,
        coroutineScope: CoroutineScope,
        action: (T) -> Unit
    ): (T) -> Unit {
        var debounceJob: Job? = null
        return { param: T ->
            debounceJob?.cancel()
            debounceJob = coroutineScope.launch {
                delay(delayMillis)
                action(param)
            }
        }
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
