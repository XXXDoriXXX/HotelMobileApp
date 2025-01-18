package com.example.hotelapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.Holder.HotelHolder

class RoomsListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rooms_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val hotelname:TextView = findViewById(R.id.hotel_name)
        hotelname.text = HotelHolder.currentHotel?.name
        val itemsList: RecyclerView =findViewById(R.id.rooms_list)
        itemsList.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        itemsList.adapter = ItemsRoomAdapter(HotelHolder.currentHotel?.rooms ?: emptyList(), this)
        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }
}