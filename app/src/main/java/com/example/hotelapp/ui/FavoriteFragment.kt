package com.example.hotelapp.ui

import HotelItem
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hotelapp.R
import com.example.hotelapp.classes.Adapters.ItemsHotelAdapter
import com.facebook.shimmer.ShimmerFrameLayout
import androidx.fragment.app.viewModels
import com.example.viewmodels.FavoriteViewModel
import com.example.viewmodels.GenericViewModelFactory


class FavoriteFragment : Fragment() {
    private val viewModel: FavoriteViewModel by viewModels {
        GenericViewModelFactory(FavoriteViewModel::class.java) {
            FavoriteViewModel(UserHolder.getHotelRepository())
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var adapter: ItemsHotelAdapter
    private val hotelRepository by lazy { UserHolder.getHotelRepository() }
    private val favoriteHotels = mutableListOf<HotelItem>()
    private lateinit var searchInput: EditText
    private val allFavorites = mutableListOf<HotelItem>()
    private lateinit var shimmerLayout: ShimmerFrameLayout
    companion object {
        private var isDataLoaded = false
    }
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var isLoading = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        recyclerView = view.findViewById(R.id.favorites_recycler_view)
        emptyState = view.findViewById(R.id.empty_state_container)
        searchInput = view.findViewById(R.id.search_input_field)

        adapter = ItemsHotelAdapter(favoriteHotels, requireContext(), isVerticalLayout = true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFavorites(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_fav)
        swipeRefreshLayout.setOnRefreshListener {
            isLoading = true
            isDataLoaded = false
            viewModel.loadFavorites()
        }

        if (!isDataLoaded) {
            isLoading = true
            favoriteHotels.clear()
            adapter.notifyDataSetChanged()
            shimmerLayout.visibility = View.VISIBLE
            shimmerLayout.startShimmer()
            viewModel.loadFavorites()
        }
        else {
            shimmerLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            isLoading = false
            if (this::adapter.isInitialized) recyclerView.adapter = adapter
            allFavorites.clear()
            allFavorites.addAll(UserHolder.favoriteHotels)

            filterFavorites(searchInput.text.toString())
        }

        viewModel.favorites.observe(viewLifecycleOwner) { hotels ->
            isLoading = false
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false

            UserHolder.favoriteHotels = hotels.toMutableList()
            allFavorites.clear()
            allFavorites.addAll(hotels)
            filterFavorites(searchInput.text.toString())
            isDataLoaded = true
        }

        viewModel.error.observe(viewLifecycleOwner) {
            isLoading = false
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
            allFavorites.clear()
            filterFavorites("")
            Toast.makeText(requireContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show()
        }



        return view
    }


    private fun filterFavorites(query: String) {
        val filtered = if (query.isBlank()) {
            allFavorites
        } else {
            allFavorites.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        favoriteHotels.clear()
        favoriteHotels.addAll(filtered)
        adapter.notifyDataSetChanged()

        recyclerView.visibility = if (filtered.isNotEmpty()) View.VISIBLE else View.GONE
        emptyState.visibility = if (!isLoading && filtered.isEmpty()) View.VISIBLE else View.GONE
    }



}
