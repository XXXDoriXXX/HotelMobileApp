package com.example.hotelapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryFragment : Fragment() {

    private lateinit var orderHistoryRecyclerView: RecyclerView
    private lateinit var adapter: ItemHistoryAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        orderHistoryRecyclerView = view.findViewById(R.id.order_history_recycler_view)
        HotelHolder.orders = listOf(
            OrderItem("Blue Star Hotel", "VIP", "Jan 10", "Jan 15", 999f),
            OrderItem("Grand Palace", "Standard", "Feb 5", "Feb 10", 499f),
            OrderItem("Luxury Stay", "Premium", "Mar 1", "Mar 5", 899f)
        ).toMutableList()

        orderHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        orderHistoryRecyclerView.adapter = ItemHistoryAdapter(HotelHolder.orders,requireContext())

        return view
    }
    fun addOrderToList(order: OrderItem) {
        adapter.addOrder(order) // Оновлюємо список у адаптері
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
