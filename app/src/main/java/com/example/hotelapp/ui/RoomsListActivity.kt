package com.example.hotelapp.ui

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.R
import com.example.hotelapp.classes.ItemsRoomAdapter
import com.example.hotelapp.classes.RoomItem
import com.example.hotelapp.repository.RoomRepository
import com.facebook.shimmer.ShimmerFrameLayout

class RoomsListActivity : AppCompatActivity() {
    private val roomRepository = RoomRepository()
    private lateinit var itemsList: RecyclerView
    private lateinit var searchInputField: EditText
    private var currentSort: String = "none"
    private var allRooms: List<RoomItem> = emptyList()
    private lateinit var shimmerContainer: ShimmerFrameLayout
    private lateinit var emptyRoomsIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rooms_list)
        shimmerContainer = findViewById(R.id.rooms_shimmer_container)
        emptyRoomsIcon = findViewById(R.id.empty_rooms_icon)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<TextView>(R.id.low_cost_tab).setOnClickListener { applyFilters("low") }
        findViewById<TextView>(R.id.average_tab).setOnClickListener { applyFilters("average") }
        findViewById<TextView>(R.id.luxury_tab).setOnClickListener { applyFilters("high") }

        val hotelName: TextView = findViewById(R.id.hotel_name)
        hotelName.text = HotelHolder.currentHotel?.name

        itemsList = findViewById(R.id.rooms_list)
        itemsList.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        itemsList.adapter = ItemsRoomAdapter(emptyList(), this)

        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        searchInputField = findViewById(R.id.search_input_field)
        loadRooms()
    }
    private fun applyFilters(sort: String) {
        currentSort = sort
        val query = searchInputField.text.toString().trim().lowercase()

        val filtered = allRooms.filter { room ->
            room.room_type.lowercase().contains(query)
        }

        val sorted = when (sort) {
            "low" -> filtered.sortedBy { it.price_per_night }
            "average" -> filtered.sortedBy { kotlin.math.abs(it.price_per_night - averagePrice(allRooms)) }
            "high" -> filtered.sortedByDescending { it.price_per_night }
            else -> filtered
        }

        itemsList.adapter = ItemsRoomAdapter(sorted, this)
    }

    private fun averagePrice(rooms: List<RoomItem>): Double {
        return rooms.map { it.price_per_night }.average()
    }
    private fun loadRooms() {
        shimmerContainer.visibility = View.VISIBLE
        shimmerContainer.startShimmer()
        itemsList.visibility = View.GONE
        emptyRoomsIcon.visibility = View.GONE

        val hotelId = HotelHolder.currentHotel?.id ?: return

        roomRepository.getRooms(hotelId) { rooms, error ->
            shimmerContainer.stopShimmer()
            shimmerContainer.visibility = View.GONE

            if (rooms != null && rooms.isNotEmpty()) {
                allRooms = rooms
                itemsList.visibility = View.VISIBLE
                applyFilters(currentSort)
            } else {
                itemsList.adapter = ItemsRoomAdapter(emptyList(), this)
                emptyRoomsIcon.visibility = View.VISIBLE
            }
        }
    }


}
