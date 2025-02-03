package com.example.hotelapp

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hotelapp.classes.ItemsHotelAdapter
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var searchInputField: TextInputEditText
    private lateinit var itemsList: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
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
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
            itemsList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            loadDefaultHotels()
            swipeRefreshLayout.setOnRefreshListener {
                refreshHotels()
                searchInputField.text = null;
            }
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

    private fun refreshHotels() {
        loadDefaultHotels()
    }

    private fun loadDefaultHotels() {
        hotelRepository.getHotels(
            onResult = { hotels ->
                if (isAdded) {
                    itemsList.adapter = ItemsHotelAdapter(hotels, requireContext())
                    swipeRefreshLayout.isRefreshing = false
                }
            },
            onError = { error ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    swipeRefreshLayout.isRefreshing = false
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
