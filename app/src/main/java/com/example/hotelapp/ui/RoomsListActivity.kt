package com.example.hotelapp.ui

import android.os.Bundle
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
import com.example.hotelapp.repository.RoomRepository

class RoomsListActivity : AppCompatActivity() {
    private val roomRepository = RoomRepository()
    private lateinit var itemsList: RecyclerView
    private lateinit var searchInputField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rooms_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

    private fun loadRooms() {
        val hotelId = HotelHolder.currentHotel?.id ?: return

        roomRepository.getRooms(hotelId) { rooms, error ->
            if (rooms != null) {
                itemsList.adapter = ItemsRoomAdapter(rooms, this)
            } else {
                Toast.makeText(this, "Failed to load rooms.", Toast.LENGTH_SHORT).show()
                itemsList.adapter = ItemsRoomAdapter(emptyList(), this)
            }
        }
    }


}
