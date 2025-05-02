package com.example.hotelapp.ui

import HotelItem
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hotelapp.R
import com.example.hotelapp.classes.ItemsHotelAdapter
import com.google.android.material.textfield.TextInputEditText
import com.example.hotelapp.classes.FiltersAdapter
import com.example.hotelapp.models.HotelSearchParams
import com.facebook.shimmer.ShimmerFrameLayout

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
    private var isDataLoaded = false
    private val pageSize = 25
    private val allHotels = mutableListOf<HotelItem>()
    private var isLoading = false
    private var canLoadMore = true
    private lateinit var filtersRecycler: RecyclerView
    private lateinit var filterButton: ImageView
    private val activeFilters = mutableMapOf<String, String>()
    private var pendingFilterType: String? = null
    private lateinit var filtersAdapter: FiltersAdapter
    private val filterList = mutableListOf<Pair<String, String>>()
    private val allFilterTypes = mutableListOf(
        "Name",
        "Description",
        "City",
        "Country",
        "State",
        "Postal_Code",
        "Min_Price",
        "Max_Price",
        "Min_Rating",
        "Room_Type",
        "Amenity_Ids",
        "Check_In",
        "Check_Out"
    )
    private lateinit var shimmerLayoutHome: ShimmerFrameLayout
    private var isSearching = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val address = prefs.getString("last_location", "Unknown")

        val tabTrending = view.findViewById<TextView>(R.id.tab_trending)
        val tabBest = view.findViewById<TextView>(R.id.tab_best)
        val tabPopular = view.findViewById<TextView>(R.id.tab_popular)
        val tabs = listOf(tabTrending, tabBest, tabPopular)
        view.findViewById<TextView>(R.id.location_text)?.text = address
        shimmerLayoutHome = view.findViewById(R.id.shimmerLayoutHome)
        itemsList = view.findViewById(R.id.itemsHotelList)

        if (!isDataLoaded) {
            shimmerLayoutHome.startShimmer()
            shimmerLayoutHome.visibility = View.VISIBLE
            itemsList.visibility = View.GONE
        } else {
            shimmerLayoutHome.visibility = View.GONE
            itemsList.visibility = View.VISIBLE
        }


        filtersRecycler = view.findViewById(R.id.filters_recycler)
        filtersAdapter = FiltersAdapter(filterList) { removedKey ->
            activeFilters.remove(removedKey)
            allFilterTypes.add(removedKey.replaceFirstChar { it.uppercase() })
            isSearching = true
            applyFilters()
            if (filterList.isEmpty()) filtersRecycler.visibility = View.GONE
        }
        filtersRecycler.adapter = filtersAdapter
        filtersRecycler.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        filterButton = view.findViewById(R.id.filter_button)
        filterButton.setOnClickListener { showFilterPopupWindow(it) }

        layoutToggleButton = view.findViewById(R.id.layoutToggleButton)
        searchInputField = view.findViewById(R.id.search_input_field)
        itemsList = view.findViewById(R.id.itemsHotelList)
        if (this::hotelAdapter.isInitialized) {
            itemsList.adapter = hotelAdapter
            updateLayoutManager()
        }

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        itemsList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        pendingFilterType = "Name"
        searchInputField.hint = "Enter Name..."

        layoutToggleButton.setOnClickListener { toggleLayout() }

        searchInputField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            ) {
                val inputValue = searchInputField.text.toString().trim()
                if (inputValue.isNotEmpty()) {
                    val filterName = pendingFilterType ?: filterList.lastOrNull()?.first
                    if (!filterName.isNullOrEmpty()) {
                        val existingIndex = filterList.indexOfFirst { it.first == filterName }
                        if (existingIndex >= 0) {
                            filterList[existingIndex] = Pair(filterName, inputValue)
                            activeFilters[filterName.lowercase()] = inputValue
                            filtersAdapter.notifyItemChanged(existingIndex)
                        } else {
                            activeFilters[filterName.lowercase()] = inputValue
                            filterList.add(Pair(filterName, inputValue))
                            allFilterTypes.remove(filterName)
                            filtersAdapter.notifyItemInserted(filterList.size - 1)
                            filtersRecycler.visibility = View.VISIBLE
                            filtersRecycler.smoothScrollToPosition(filterList.size - 1)
                        }
                        searchInputField.text = null
                        pendingFilterType = null
                        searchInputField.hint = "Search..."
                        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)
                                as android.view.inputmethod.InputMethodManager
                        imm.hideSoftInputFromWindow(searchInputField.windowToken, 0)
                        isSearching = true
                        applyFilters()
                    }
                }
                true
            } else false
        }

        swipeRefreshLayout.setOnRefreshListener {
            refreshHotels()
            searchInputField.text = null
        }

        itemsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val centerX = recyclerView.width / 2
                for (i in 0 until recyclerView.childCount) {
                    val child = recyclerView.getChildAt(i)
                    val childCenter = (child.left + child.right) / 2
                    val d = Math.min(Math.abs(centerX - childCenter).toFloat(), centerX.toFloat())
                    val scale = 1f - 0.05f * (d / centerX)
                    child.scaleX = scale
                    child.scaleY = scale
                    child.alpha = 0.5f + 0.5f * scale
                }

                if (!isSearching) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    if (!isLoading && canLoadMore && lastVisibleItem >= totalItemCount - 5) {
                        loadMoreHotels()
                    }
                }
            }
        })

        tabTrending.isSelected = true
        currentCategory = "trending"
        currentPage = 0
        if (!isSearching && !isDataLoaded) {
            loadHotels()
            isDataLoaded = true
        }


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
                if (!isSearching) loadHotels()
            }
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
                background = resources.getDrawable(R.drawable.filter_popup_background, null)
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
            isSearching = false
            loadHotels()
            return
        }

        val amenityIds = activeFilters["amenity_ids"]
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }

        val searchParams = HotelSearchParams(
            name = activeFilters["name"],
            description = activeFilters["description"],
            city = activeFilters["city"],
            country = activeFilters["country"],
            state = activeFilters["state"],
            postal_code = activeFilters["postal_code"],
            min_price = activeFilters["min_price"]?.toFloatOrNull(),
            max_price = activeFilters["max_price"]?.toFloatOrNull(),
            min_rating = activeFilters["min_rating"]?.toFloatOrNull(),
            room_type = activeFilters["room_type"],
            amenity_ids = amenityIds,
            check_in = activeFilters["check_in"],
            check_out = activeFilters["check_out"],
            sort_by = activeFilters["sort_by"] ?: "rating",
            sort_dir = activeFilters["sort_dir"] ?: "desc",
            skip = 0,
            limit = 25
        )

        hotelRepository.searchHotelsByFilters(
            filters = searchParams,
            onResult = { hotels ->
                allHotels.clear()
                allHotels.addAll(hotels)
                hotelAdapter = ItemsHotelAdapter(allHotels, requireContext(), isVerticalLayout)
                itemsList.adapter = hotelAdapter
            },
            onError = { err ->
                Toast.makeText(requireContext(), "Search failed: ${err.message}", Toast.LENGTH_SHORT).show()
                isSearching = false
            }
        )
    }

    private fun refreshHotels() {
        if (isSearching) {
            swipeRefreshLayout.isRefreshing = false
            return
        }
        isDataLoaded = false
        loadHotels()
        swipeRefreshLayout.isRefreshing = true
    }


    private fun loadHotels() {
        if (isSearching) return
        isLoading = true
        currentPage = 0
        allHotels.clear()
        canLoadMore = true
        hotelAdapter = ItemsHotelAdapter(allHotels, requireContext(), isVerticalLayout)
        itemsList.adapter = hotelAdapter
        updateLayoutManager()
        loadMoreHotels()
    }

    private fun loadMoreHotels() {
        if (isSearching) return
        isLoading = true

        if (currentPage == 0) {
            shimmerLayoutHome.startShimmer()
            shimmerLayoutHome.visibility = View.VISIBLE
            itemsList.visibility = View.GONE
        }

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
                if (!isAdded) return@getHotelsByCategory

                if (hotels.isNotEmpty()) {
                    val oldSize = allHotels.size
                    allHotels.addAll(hotels)
                    hotelAdapter.notifyItemRangeInserted(oldSize, hotels.size)
                    currentPage++
                }
                if (hotels.size < pageSize) {
                    canLoadMore = false
                }
                isLoading = false
                swipeRefreshLayout.isRefreshing = false

                shimmerLayoutHome.stopShimmer()
                shimmerLayoutHome.visibility = View.GONE
                itemsList.visibility = View.VISIBLE
            },
            onError = {
                if (!isAdded) return@getHotelsByCategory
                isLoading = false
                swipeRefreshLayout.isRefreshing = false

                shimmerLayoutHome.stopShimmer()
                shimmerLayoutHome.visibility = View.GONE
                itemsList.visibility = View.VISIBLE

                Toast.makeText(requireContext(), "Помилка підвантаження", Toast.LENGTH_SHORT).show()
            }
        )
    }


    private fun updateLayoutManager() {
        val oldPosition = (itemsList.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: 0
        val layoutManager = if (isVerticalLayout) {
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        } else {
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
        itemsList.layoutManager = layoutManager
        itemsList.scrollToPosition(oldPosition)
    }

    private fun toggleLayout() {
        isVerticalLayout = !isVerticalLayout
        updateLayoutManager()
        if (isSearching) {
            applyFilters()
        } else {
            loadHotels()
        }
    }
}
