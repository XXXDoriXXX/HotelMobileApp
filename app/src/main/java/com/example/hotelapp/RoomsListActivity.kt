package com.example.hotelapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.repository.RoomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        searchInputField = findViewById(R.id.search_input_field)

        val debounceSearch = debounce<String>(
            delayMillis = 500L,
            coroutineScope = lifecycleScope
        ) { query ->
            performSearch(query)
        }

        searchInputField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                debounceSearch(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        loadRooms("")
    }

    private fun loadRooms(query: String) {
        val hotelId = HotelHolder.currentHotel?.id ?: return

        roomRepository.searchRooms(hotelId, query,
            callback = { rooms, error ->

                if (rooms != null) {
                    if (rooms.isNotEmpty()) {
                        itemsList.adapter = ItemsRoomAdapter(rooms, this)
                    }
                }
                else {
                    Toast.makeText(this, "No rooms found for your search.", Toast.LENGTH_SHORT).show()
                    itemsList.adapter = ItemsRoomAdapter(emptyList(), this)
                }

            }
        )
    }

    private fun performSearch(query: String) {
        loadRooms(query)
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
}
