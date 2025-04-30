package com.example.hotelapp

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
import com.example.hotelapp.classes.ItemsHotelAdapter
import com.facebook.shimmer.ShimmerFrameLayout

class FavoriteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var adapter: ItemsHotelAdapter
    private val hotelRepository by lazy { UserHolder.getHotelRepository() }
    private val favoriteHotels = mutableListOf<HotelItem>()
    private lateinit var searchInput: EditText
    private val allFavorites = mutableListOf<HotelItem>()
    private lateinit var shimmerLayout: ShimmerFrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        shimmerLayout.startShimmer()

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

        loadFavorites()
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

        recyclerView.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadFavorites() {

        hotelRepository.getFavorites(
            onResult = { hotels ->
                allFavorites.clear()
                allFavorites.addAll(hotels)
                if (hotels.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE
                    filterFavorites(searchInput.text.toString())
                }

                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show()
                error.printStackTrace()
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
            }
        )
    }

}
