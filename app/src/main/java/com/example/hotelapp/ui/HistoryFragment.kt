package com.example.hotelapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        val apiService = RetrofitClient.retrofit.create(HotelService::class.java)
        val repository = BookingRepository(apiService, SessionManager(requireContext()))
        orderHistoryRecyclerView = view.findViewById(R.id.order_history_recycler_view)
        adapter = ItemHistoryAdapter(HotelHolder.orders, requireContext())
        shimmerLayout = view.findViewById(R.id.shimmerLayout)
        orderHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        orderHistoryRecyclerView.adapter = adapter
        shimmerLayout.startShimmer()
        shimmerLayout.visibility = View.VISIBLE
        orderHistoryRecyclerView.visibility = View.GONE

        repository.getMyBookings(
            onResult = { bookings ->
                if (isAdded) {
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
                            room_id = it.room_id
                        )
                    }.toMutableList()

                    HotelHolder.orders = orderItems

                    adapter = ItemHistoryAdapter(orderItems, requireContext())
                    orderHistoryRecyclerView.adapter = adapter
                }
            },
            onError = {
                if (isAdded) {
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    orderHistoryRecyclerView.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Не вдалося завантажити бронювання", Toast.LENGTH_SHORT).show()
                }
            }
        )


        return view
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    fun addOrderToList(order: OrderItem) {
        HotelHolder.orders.add(order)
        adapter.notifyItemInserted(HotelHolder.orders.size - 1)
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
