package com.example.hotelapp

import HotelItem
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
import android.widget.PopupMenu
import android.widget.PopupWindow
import com.example.hotelapp.classes.FiltersAdapter

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
    private lateinit var filtersRecycler: RecyclerView
    private lateinit var filterButton: ImageView
    private val activeFilters = mutableMapOf<String, String>()
    private var pendingFilterType: String? = null
    private lateinit var filtersAdapter: FiltersAdapter
    private val filterList = mutableListOf<Pair<String, String>>()
    private val allFilterTypes = mutableListOf("Name", "Rating", "Views", "Comfort", "Address")

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
            val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val address = prefs.getString("last_location", "Unknown")

            val tabTrending = view.findViewById<TextView>(R.id.tab_trending)
            val tabBest = view.findViewById<TextView>(R.id.tab_best)
            val tabPopular = view.findViewById<TextView>(R.id.tab_popular)
            view.findViewById<TextView>(R.id.location_text)?.text = address
            val tabs = listOf(tabTrending, tabBest, tabPopular)
            filtersRecycler = view.findViewById(R.id.filters_recycler)
            filtersAdapter = FiltersAdapter(filterList) { removedKey ->
                activeFilters.remove(removedKey)
                allFilterTypes.add(removedKey.replaceFirstChar { it.uppercase() })
                applyFilters()
                if (filterList.isEmpty()) filtersRecycler.visibility = View.GONE
            }

            filtersRecycler.adapter = filtersAdapter
            filtersRecycler.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

            filterButton = view.findViewById(R.id.filter_button)

            filterButton.setOnClickListener {
                showFilterPopupWindow(it)
            }
            val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            layoutManager.reverseLayout = true
            layoutManager.stackFromEnd = true
            filtersRecycler.layoutManager = layoutManager

            layoutToggleButton = view.findViewById(R.id.layoutToggleButton)
            searchInputField = view.findViewById(R.id.search_input_field)
            pendingFilterType = "Name"
            searchInputField.hint = "Enter Name..."
            itemsList = view.findViewById(R.id.itemsHotelList)
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
            itemsList.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

            layoutToggleButton.setOnClickListener {
                toggleLayout()
            }
            searchInputField.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
                ) {
                    val inputValue = searchInputField.text.toString().trim()

                    if (inputValue.isNotEmpty()) {
                        if (pendingFilterType != null) {
                            activeFilters[pendingFilterType!!.lowercase()] = inputValue
                            filterList.add(Pair(pendingFilterType!!, inputValue))
                            allFilterTypes.remove(pendingFilterType!!)
                            filtersAdapter.notifyItemInserted(filterList.size - 1)
                            filtersRecycler.visibility = View.VISIBLE
                            filtersRecycler.smoothScrollToPosition(filterList.size - 1)
                            pendingFilterType = null
                            searchInputField.hint = "Search..."
                        } else {
                            val last = filterList.lastOrNull()
                            if (last != null) {
                                val index = filterList.size - 1
                                val key = last.first
                                filterList[index] = Pair(key, inputValue)
                                activeFilters[key.lowercase()] = inputValue
                                filtersAdapter.notifyItemChanged(index)
                            }
                        }

                        searchInputField.text = null

                        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)
                                as android.view.inputmethod.InputMethodManager
                        imm.hideSoftInputFromWindow(searchInputField.windowToken, 0)

                        applyFilters()
                    }

                    true
                } else false
            }


            searchInputField.hint = if (pendingFilterType != null) "Enter ${pendingFilterType}..." else "Search..."

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
    private fun showFilterPopupWindow(anchor: View) {
        val popupView = layoutInflater.inflate(R.layout.layout_filter_popup, null) as LinearLayout
        popupView.removeAllViews()

        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        for (filter in allFilterTypes) {
            val item = TextView(requireContext()).apply {
                text = filter
                setPadding(32, 24, 32, 24)
                textSize = 16f
                setTextColor(resources.getColor(R.color.colorOnPrimary, null))
                background = resources.getDrawable(R.drawable.light_rounded_corners, null)
                setOnClickListener {
                    pendingFilterType = filter
                    searchInputField.hint = "Enter $filter..."
                    searchInputField.requestFocus()
                    popupWindow.dismiss()
                }
            }
            popupView.addView(item)
        }

        popupWindow.elevation = 12f
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(null)
        popupWindow.showAsDropDown(anchor, -8, 16)
    }




    private fun applyFilters() {
        if (activeFilters.isEmpty()) {
            loadHotels()
            return
        }

        // TODO: Викликати backend з activeFilters
        Toast.makeText(requireContext(), "Applied filters: $activeFilters", Toast.LENGTH_SHORT).show()

        // Наприклад:
        // hotelRepository.searchHotelsByFilters(activeFilters, onResult = {...})
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

        val address = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getString("last_location", "Unknown City, Unknown Country") ?: "Unknown City, Unknown Country"

        val parts = address.split(",").map { it.trim() }
        val city = parts.getOrNull(0) ?: "Unknown"
        val country = parts.getOrNull(1) ?: "Unknown"

        hotelRepository.getHotelsByCategory(
            category = currentCategory,
            city = city,
            country = country,
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