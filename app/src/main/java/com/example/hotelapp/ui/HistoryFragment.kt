package com.example.hotelapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.R
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.classes.ItemHistoryAdapter
import com.example.hotelapp.classes.OrderItem
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.repository.BookingRepository
import com.example.hotelapp.utils.SessionManager
import com.facebook.shimmer.ShimmerFrameLayout

class HistoryFragment : Fragment() {

    private lateinit var orderHistoryRecyclerView: RecyclerView
    private lateinit var adapter: ItemHistoryAdapter
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var sortSpinner: Spinner
    private lateinit var repository: BookingRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        val apiService = RetrofitClient.retrofit.create(HotelService::class.java)
        repository = BookingRepository(apiService, SessionManager(requireContext()))
        sortSpinner = view.findViewById(R.id.sort_spinner)

        val items = resources.getStringArray(R.array.sort_options)
        val adapterspiner = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_dropdown_item2,
            R.id.dropdown_item_text,
            items
        ) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return super.getDropDownView(position, convertView, parent)
            }
        }


        sortSpinner.adapter = adapterspiner

        orderHistoryRecyclerView = view.findViewById(R.id.order_history_recycler_view)
        shimmerLayout = view.findViewById(R.id.shimmerLayout)
        sortSpinner = view.findViewById(R.id.sort_spinner)

        adapter = ItemHistoryAdapter(mutableListOf(), requireContext())
        orderHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        orderHistoryRecyclerView.adapter = adapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> loadBookings("created_at", "asc")
                    1 -> loadBookings("created_at", "desc")
                    2 -> loadBookings("status", "asc")
                    3 -> loadBookings("status", "desc")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        loadBookings("created_at", "desc")
        return view
    }

    private fun loadBookings(sortBy: String, order: String) {
        shimmerLayout.startShimmer()
        shimmerLayout.visibility = View.VISIBLE
        orderHistoryRecyclerView.visibility = View.GONE

        repository.getMyBookingsSorted(
            sortBy = sortBy,
            order = order,
            onResult = { bookings ->
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                orderHistoryRecyclerView.visibility = View.VISIBLE

                val orderItems = bookings.map {
                    OrderItem(
                        bookingId = it.booking_id,
                        hotelName = it.hotel_name,
                        roomType = it.room_type,
                        checkInDate = it.date_start.substring(0, 10),
                        checkOutDate = it.date_end.substring(0, 10),
                        totalPrice = it.total_price,
                        status = mapStatus(it.status),
                        hotel_image_url = it.hotel_images?.firstOrNull()?.image_url ?: "",
                        room_id = it.room_id,
                        createdAt = it.created_at.substring(0, 10)
                    )
                }

                val grouped = mutableListOf<Any>()
                orderItems.groupBy { it.createdAt }.forEach { (date, list) ->
                    grouped.add(date)
                    grouped.addAll(list)
                }

                adapter = ItemHistoryAdapter(grouped.toMutableList(), requireContext())
                orderHistoryRecyclerView.adapter = adapter
            },
            onError = {
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                orderHistoryRecyclerView.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Не вдалося завантажити бронювання", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }

    private fun mapStatus(status: String?): String {
        return when (status?.lowercase()) {
            "confirmed" -> "Confirmed"
            "pending_payment", "awaiting_confirmation" -> "Pending"
            "cancelled" -> "Cancelled"
            else -> "Unknown"
        }
    }
}
