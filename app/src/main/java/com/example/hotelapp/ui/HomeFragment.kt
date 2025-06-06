package com.example.hotelapp.ui

import HotelItem
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hotelapp.Holder.AmenityHolder
import com.example.hotelapp.R
import com.example.hotelapp.classes.Adapters.ItemsHotelAdapter
import com.google.android.material.textfield.TextInputEditText
import com.example.hotelapp.classes.Adapters.FiltersAdapter
import com.example.hotelapp.classes.SnackBarUtils
import com.example.hotelapp.models.HotelSearchParams
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.fragment.app.viewModels
import com.example.viewmodels.HotelViewModel
import com.example.viewmodels.GenericViewModelFactory

class HomeFragment : Fragment() {
    private val viewModel: HotelViewModel by viewModels {
        GenericViewModelFactory(HotelViewModel::class.java) {
            HotelViewModel(UserHolder.getHotelRepository())
        }
    }

    private lateinit var searchInputField: TextInputEditText
    private lateinit var itemsList: RecyclerView
    private lateinit var layoutToggleButton: ImageButton
    private var isVerticalLayout = false
    private lateinit var hotelAdapter: ItemsHotelAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
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
        "Check_Date"
    )

    val filterDisplayNames = mapOf(
        "Name" to R.string.filter_name,
        "Description" to R.string.filter_description,
        "City" to R.string.filter_city,
        "Country" to R.string.filter_country,
        "State" to R.string.filter_state,
        "Postal_Code" to R.string.filter_postal_code,
        "Min_Price" to R.string.filter_min_price,
        "Max_Price" to R.string.filter_max_price,
        "Min_Rating" to R.string.filter_min_rating,
        "Room_Type" to R.string.filter_room_type,
        "Amenity_Ids" to R.string.filter_amenities,
        "Check_Date" to R.string.filter_check_date
    )


    private val amenityNameToId = mutableMapOf<String, Int>()
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
        viewModel.hotels.observe(viewLifecycleOwner) { hotels ->
            if (!this::hotelAdapter.isInitialized) {
                hotelAdapter = ItemsHotelAdapter(hotels.toMutableList(), requireContext(), isVerticalLayout)
                itemsList.adapter = hotelAdapter
            } else {
                hotelAdapter.items = hotels.toMutableList()
                hotelAdapter.notifyDataSetChanged()
            }

            shimmerLayoutHome.stopShimmer()
            shimmerLayoutHome.visibility = View.GONE
            itemsList.visibility = View.VISIBLE
            isLoading = false
            swipeRefreshLayout.isRefreshing = false
            if (hotels.isNotEmpty()) {
                currentPage++
            }
            canLoadMore = hotels.size == pageSize

        }

        viewModel.amenities.observe(viewLifecycleOwner) { list ->
            amenityNameToId.clear()
            amenityNameToId.putAll(list.associate { it.name to it.id })
            AmenityHolder.allAmenities = list
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                shimmerLayoutHome.startShimmer()
                shimmerLayoutHome.visibility = View.VISIBLE
                itemsList.visibility = View.GONE
            } else {
                shimmerLayoutHome.stopShimmer()
                shimmerLayoutHome.visibility = View.GONE
                itemsList.visibility = View.VISIBLE
            }

        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                SnackBarUtils.showLong(requireContext(), requireView(), R.string.loading_error, it)
            }
        }

        viewModel.loadAllAmenities()

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
        filtersAdapter = FiltersAdapter(
            filters = filterList,
            onRemove = { removedKey ->
                if (removedKey.lowercase() == "check_date") {
                    activeFilters.remove("check_in")
                    activeFilters.remove("check_out")
                } else {
                    activeFilters.remove(removedKey.lowercase())
                }

                filterList.removeAll { it.first.lowercase() == removedKey.lowercase() }

                filtersAdapter.notifyDataSetChanged()

                if (filterList.isEmpty()) {
                    filtersRecycler.visibility = View.GONE
                    isSearching = false
                    loadHotels()
                } else {
                    isSearching = true
                    applyFilters()
                }
            },
            onUpdate = { key, newValue ->
                activeFilters[key.lowercase()] = newValue
                isSearching = true
                applyFilters()
            },
            displayNames = filterDisplayNames
        )

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
        searchInputField.hint = getString(R.string.search_enter_prefix, getString(R.string.filter_name))


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
                        searchInputField.hint =  getString(R.string.search_hint)
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
    private fun applySelectedFilter(filterName: String, value: String) {
        val lowercaseKey = filterName.lowercase()

        val uniqueKeys = setOf("amenity_ids", "check_in", "check_out")

        if (lowercaseKey in uniqueKeys) {
            val existingIndex = filterList.indexOfFirst { it.first.lowercase() == lowercaseKey }
            if (existingIndex >= 0) {
                filterList[existingIndex] = Pair(filterName, value)
                activeFilters[lowercaseKey] = value
                filtersAdapter.notifyItemChanged(existingIndex)
            } else {
                activeFilters[lowercaseKey] = value
                filterList.add(Pair(filterName, value))
                filtersAdapter.notifyItemInserted(filterList.size - 1)
                filtersRecycler.visibility = View.VISIBLE
                filtersRecycler.smoothScrollToPosition(filterList.size - 1)
            }
        } else {
            val alreadyExists = filterList.any { it.first == filterName && it.second == value }
            if (!alreadyExists) {
                activeFilters[lowercaseKey] = value
                filterList.add(Pair(filterName, value))
                filtersAdapter.notifyItemInserted(filterList.size - 1)
                filtersRecycler.visibility = View.VISIBLE
                filtersRecycler.smoothScrollToPosition(filterList.size - 1)
            }
        }

        searchInputField.text = null
        pendingFilterType = null
        searchInputField.hint = getString(R.string.search_hint)


        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(searchInputField.windowToken, 0)

        isSearching = true
        applyFilters()
    }



    private fun showFilterPopupWindow(anchor: View) {
        val popupView = layoutInflater.inflate(R.layout.layout_filter_popup, null)
        val filterContainer = popupView.findViewById<LinearLayout>(R.id.filter_list_container)
        filterContainer.removeAllViews()

        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        for (filter in allFilterTypes) {
            val item = TextView(requireContext()).apply {
                text = getString(filterDisplayNames[filter] ?: R.string.unknown_filter)

                setPadding(32, 24, 32, 24)
                textSize = 16f
                setTextColor(resources.getColor(R.color.colorOnPrimary, null))
                background = resources.getDrawable(R.drawable.filter_popup_background, null)

                setOnClickListener {
                    when (filter.lowercase()) {
                        "check_date" -> {
                            val validator = CompositeDateValidator.allOf(listOf(DateValidatorPointForward.now()))
                            val constraints = CalendarConstraints.Builder().setValidator(validator).build()

                            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                                .setTitleText(getString(R.string.filter_select_date))
                                .setCalendarConstraints(constraints)
                                .build()

                            dateRangePicker.show(parentFragmentManager, "date_range_picker")

                            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                                val startDate = selection.first
                                val endDate = selection.second

                                if (startDate != null && endDate != null) {
                                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val checkIn = formatter.format(Date(startDate))
                                    val checkOut = formatter.format(Date(endDate))

                                    activeFilters["check_in"] = checkIn
                                    activeFilters["check_out"] = checkOut

                                    val displayLabel = "$checkIn → $checkOut"
                                    val existingIndex = filterList.indexOfFirst { it.first == "Check_Date" }

                                    if (existingIndex >= 0) {
                                        filterList[existingIndex] = Pair("Check_Date", displayLabel)
                                        filtersAdapter.notifyItemChanged(existingIndex)
                                    } else {
                                        filterList.add(Pair("Check_Date", displayLabel))
                                        filtersAdapter.notifyItemInserted(filterList.size - 1)
                                        filtersRecycler.visibility = View.VISIBLE
                                        filtersRecycler.smoothScrollToPosition(filterList.size - 1)
                                    }

                                    isSearching = true
                                    applyFilters()
                                }
                            }
                        }


                        "min_price", "max_price", "min_rating", "postal_code" -> {
                            val hintKey = filterDisplayNames[filter]
                            searchInputField.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                            searchInputField.hint = getString(R.string.search_enter_prefix, getString(hintKey ?: R.string.unknown_filter))
                            pendingFilterType = filter
                            searchInputField.requestFocus()
                        }

                        "amenity_ids" -> {
                            val amenityNames = amenityNameToId.keys.toList()
                            val selected = BooleanArray(amenityNames.size) { index ->
                                val current = activeFilters["amenity_ids"]?.split(",")?.map { it.trim() } ?: emptyList()
                                amenityNames[index] in current
                            }

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.filter_select_amenities))
                                .setMultiChoiceItems(amenityNames.toTypedArray(), selected) { _, which, isChecked ->
                                    selected[which] = isChecked
                                }
                                .setPositiveButton(getString(R.string.filter_apply)) { _, _ ->
                                    val selectedAmenities = amenityNames.filterIndexed { index, _ -> selected[index] }
                                    applySelectedFilter("Amenity_Ids", selectedAmenities.joinToString(", "))
                                }
                                .setNegativeButton(getString(R.string.filter_cancel), null)
                                .show()
                        }

                        else -> {
                            val hintKey = filterDisplayNames[filter]
                            searchInputField.inputType = InputType.TYPE_CLASS_TEXT
                            searchInputField.hint = getString(R.string.search_enter_prefix, getString(hintKey ?: R.string.unknown_filter))
                            pendingFilterType = filter
                            searchInputField.requestFocus()
                        }
                    }
                    popupWindow.dismiss()
                }
            }
            filterContainer.addView(item)

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
            ?.mapNotNull { amenityNameToId[it.trim()] }
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
        val gson = Gson()
        Log.d("FILTER_BODY", gson.toJson(searchParams))
        viewModel.searchHotelsByFilters(searchParams)

    }

    private fun refreshHotels() {
        if (isSearching) {
            swipeRefreshLayout.isRefreshing = false
            return
        }
        isDataLoaded = false
        loadHotels()
    }


    private fun loadHotels() {
        if (isSearching) return

        if (this::hotelAdapter.isInitialized) {
            hotelAdapter.items.clear()
            hotelAdapter.notifyDataSetChanged()
        }
        isLoading = true
        currentPage = 0
        allHotels.clear()
        canLoadMore = true

        viewModel.loadHotelsByCategory(
            category = currentCategory,
            city = getCity(),
            country = getCountry(),
            skip = 0,
            limit = pageSize
        )
    }

    private fun getCity(): String {
        val address = requireContext()
            .getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getString("last_location", "Unknown City, Unknown Country") ?: ""
        return address.split(",").getOrNull(0)?.trim() ?: "Unknown"
    }

    private fun getCountry(): String {
        val address = requireContext()
            .getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getString("last_location", "Unknown City, Unknown Country") ?: ""
        return address.split(",").getOrNull(1)?.trim() ?: "Unknown"
    }

    private fun loadMoreHotels() {
        if (isSearching || isLoading || !canLoadMore) return

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

        viewModel.loadHotelsByCategory(
            category = currentCategory,
            city = city,
            country = country,
            skip = currentPage * pageSize,
            limit = pageSize
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
        hotelAdapter = ItemsHotelAdapter(hotelAdapter.items, requireContext(), isVerticalLayout)
        itemsList.adapter = hotelAdapter
        if (isSearching) {
            applyFilters()
        } else {
            loadHotels()
        }
    }
}
