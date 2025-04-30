package com.example.hotelapp

import HotelItem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.classes.ItemsHotelAdapter

class FavoriteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var adapter: ItemsHotelAdapter
    private val hotelRepository by lazy { UserHolder.getHotelRepository() }
    private val favoriteHotels = mutableListOf<HotelItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        recyclerView = view.findViewById(R.id.favorites_recycler_view)
        emptyState = view.findViewById(R.id.empty_state_container)

        adapter = ItemsHotelAdapter(favoriteHotels, requireContext(), isVerticalLayout = true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadFavorites()

        return view
    }

    private fun loadFavorites() {
        hotelRepository.getFavorites(
            onResult = { hotels ->
                favoriteHotels.clear()
                favoriteHotels.addAll(hotels)
                adapter.notifyDataSetChanged()

                if (hotels.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE
                }
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show()
                error.printStackTrace()
            }
        )
    }
}
