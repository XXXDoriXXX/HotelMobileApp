package com.example.hotelapp

import HotelItem
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
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
    private lateinit var layoutToggleButton: ImageButton
    private var isVerticalLayout = false
    private lateinit var hotelAdapter: ItemsHotelAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val hotelRepository = UserHolder.getHotelRepository()
    private var currentCategory = "trending"
    private var currentPage = 0
    private val pageSize = 25
    private val allHotels = mutableListOf<HotelItem>()
    private var isLoading = false
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
            val tabTrending = view.findViewById<TextView>(R.id.tab_trending)
            val tabBest = view.findViewById<TextView>(R.id.tab_best)
            val tabPopular = view.findViewById<TextView>(R.id.tab_popular)

            val tabs = listOf(tabTrending, tabBest, tabPopular)

            layoutToggleButton = view.findViewById(R.id.layoutToggleButton)
            searchInputField = view.findViewById(R.id.search_input_field)
            itemsList = view.findViewById(R.id.itemsHotelList)
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
            itemsList.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

            layoutToggleButton.setOnClickListener {
                toggleLayout()
            }

            swipeRefreshLayout.setOnRefreshListener {
                refreshHotels()
                searchInputField.text = null
            }

            itemsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (!isLoading && lastVisibleItem >= totalItemCount - 5) {
                        loadMoreHotels()
                    }
                }
            })

            // Після ініціалізації ВСЬОГО — таби та loadHotels
            tabTrending.isSelected = true
            currentCategory = "trending"
            currentPage = 0
            loadHotels()

            tabs.forEach { tab ->
                tab.setOnClickListener {
                    tabs.forEach { it.isSelected = false }
                    tab.isSelected = true

                    when (tab.id) {
                        R.id.tab_trending -> currentCategory = "trending"
                        R.id.tab_best -> currentCategory = "best"
                        R.id.tab_popular -> currentCategory = "popular"
                    }

                    currentPage = 0
                    loadHotels()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error initializing views: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun refreshHotels() {
        loadHotels()
        swipeRefreshLayout.isRefreshing = true
    }

    private fun loadHotels() {
        currentPage = 0
        allHotels.clear()
        hotelAdapter = ItemsHotelAdapter(allHotels, requireContext(), isVerticalLayout)
        itemsList.adapter = hotelAdapter
        updateLayoutManager()
        loadMoreHotels()
    }


    private fun loadMoreHotels() {
        isLoading = true
        hotelRepository.getHotelsByCategory(
            category = currentCategory,
            skip = currentPage * pageSize,
            limit = pageSize,
            onResult = { hotels ->
                if (hotels.isNotEmpty()) {
                    val oldSize = allHotels.size
                    allHotels.addAll(hotels)
                    hotelAdapter.notifyItemRangeInserted(oldSize, hotels.size)
                    currentPage++
                }
                isLoading = false
            },
            onError = {
                isLoading = false
                Toast.makeText(requireContext(), "Помилка підвантаження", Toast.LENGTH_SHORT).show()
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
    private fun loadInitialHotels() {
        currentPage = 0
        allHotels.clear()
        loadMoreHotels()
    }

    companion object {
        fun newInstance() = HomeFragment()
    }

    private fun toggleLayout() {
        isVerticalLayout = !isVerticalLayout
        updateLayoutManager()
        reloadHotels()
    }

    private fun updateLayoutManager() {
        val layoutManager = if (isVerticalLayout) {
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        } else {
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
        itemsList.layoutManager = layoutManager
    }
    private fun reloadHotels() {
        hotelRepository.getHotels(
            onResult = { hotels ->
                if (isAdded) {
                    hotelAdapter = ItemsHotelAdapter(hotels, requireContext(), isVerticalLayout)
                    itemsList.adapter = hotelAdapter
                    swipeRefreshLayout.isRefreshing = false
                }
            },
            onError = { error ->
                if (isAdded) {
                    swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}