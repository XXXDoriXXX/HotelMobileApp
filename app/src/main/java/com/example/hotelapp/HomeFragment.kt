package com.example.hotelapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Знаходимо RecyclerView
            val itemsList: RecyclerView = view.findViewById(R.id.itemsHotelList)

            // Створюємо дані для списку
            val items = arrayListOf<HotelItem>()
            val room1 = RoomItem(1, "room101", 101, "Premium", 2, 399f, 1)
            val room2 = RoomItem(1, "room201", 201, "Vip", 2, 299f, 1)
            val room3 = RoomItem(1, "room102", 102, "Standart", 4, 199f, 1)

            items.add(
                HotelItem(
                    1,
                    "Grand Palace",
                    "grandpalace",
                    "Khmelnytskiy",
                    "Zarichanska 10/4",
                    listOf(room1, room2, room3)
                )
            )
            items.add(
                HotelItem(
                    2,
                    "Bukovel City",
                    "bukovel",
                    "Bukovel",
                    "Tsentralna 3",
                    listOf(room1, room2, room3)
                )
            )
            items.add(
                HotelItem(
                    3,
                    "Poltava Tower",
                    "poltavex",
                    "Poltava",
                    "Ivana Mazepy 6",
                    listOf(room1, room2, room3)
                )
            )

            // Налаштовуємо RecyclerView
            itemsList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            itemsList.adapter = ItemsHotelAdapter(items, requireContext())

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
