package com.example.hotelapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.R
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.classes.Adapters.ItemHistoryAdapter
import com.example.hotelapp.classes.OrderItem
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.repository.BookingRepository
import com.example.hotelapp.utils.SessionManager
import com.facebook.shimmer.ShimmerFrameLayout
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import kotlin.math.abs
import androidx.fragment.app.viewModels
import com.example.hotelapp.classes.SnackBarUtils
import com.example.viewmodels.HistoryViewModel
import com.example.viewmodels.GenericViewModelFactory

class HistoryFragment : Fragment() {
    private val viewModel: HistoryViewModel by viewModels {
        GenericViewModelFactory(HistoryViewModel::class.java) {
            HistoryViewModel(
                BookingRepository(
                    RetrofitClient.retrofit.create(HotelService::class.java),
                    UserHolder.getSessionManager()
                )
            )
        }
    }

    private lateinit var orderHistoryRecyclerView: RecyclerView
    private lateinit var adapter: ItemHistoryAdapter
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var sortSpinner: Spinner
    private lateinit var rootView: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_history, container, false)

        val apiService = RetrofitClient.retrofit.create(HotelService::class.java)
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

        viewModel.bookings.observe(viewLifecycleOwner) { bookings ->
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            orderHistoryRecyclerView.visibility = View.VISIBLE

            val grouped = mutableListOf<Any>()
            bookings.groupBy { it.createdAt }.forEach { (date, list) ->
                grouped.add(date)
                grouped.addAll(list)
            }

            adapter = ItemHistoryAdapter(
                grouped.toMutableList(),
                requireContext(),
                onDeleteBooking = { bookingId, position -> deleteBooking(bookingId, position) },
                onArchiveBooking = { bookingId, position -> archiveBooking(bookingId, position) }
            )
            orderHistoryRecyclerView.adapter = adapter
        }
        viewModel.loadBookings(sortBy = "created_at", order = "desc")
        viewModel.error.observe(viewLifecycleOwner) {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            orderHistoryRecyclerView.visibility = View.VISIBLE
            SnackBarUtils.showLong(requireContext(),rootView, R.string.toast_error_with_reason, "Не вдалося завантажити бронювання")
        }

        sortSpinner.adapter = adapterspiner
        sortSpinner.setSelection(1)

        orderHistoryRecyclerView = view.findViewById(R.id.order_history_recycler_view)
        shimmerLayout = view.findViewById(R.id.shimmerLayout)
        sortSpinner = view.findViewById(R.id.sort_spinner)
        adapter = ItemHistoryAdapter(
            mutableListOf(),
            requireContext(),
            onDeleteBooking = { _, _ -> },
            onArchiveBooking = { _, _ -> }
        )
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    return
                }

                val itemView = viewHolder.itemView
                val context = recyclerView.context
                val status = (adapter.getItem(viewHolder.bindingAdapterPosition) as? OrderItem)?.status

                val paint = Paint()
                val icon: Drawable?
                val backgroundColor: Int

                when (status) {
                    "Cancelled" -> {
                        backgroundColor = Color.parseColor("#f44336")
                        icon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
                        icon?.mutate()?.setTint(Color.parseColor("#f44336"))


                    }
                    "Completed" -> {
                        backgroundColor = Color.parseColor("#607d8b")
                        icon = ContextCompat.getDrawable(context, R.drawable.ic_archive)
                        icon?.mutate()?.setTint(Color.parseColor("#607d8b"))

                    }
                    else -> {
                        backgroundColor = Color.LTGRAY
                        icon = null
                    }
                }

                val alpha = minOf(1f, abs(dX) / itemView.width.toFloat())
                paint.color = backgroundColor
                paint.alpha = (alpha * 255).toInt()

                val background = when (status) {
                    "Cancelled" -> ContextCompat.getDrawable(context, R.drawable.swipe_background_cancelled)
                    "Completed" -> ContextCompat.getDrawable(context, R.drawable.swipe_background_completed)
                    else -> null
                }

                background?.let {
                    val bounds = Rect(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    it.bounds = bounds
                    it.draw(c)
                }

                icon?.let {
                    val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                    val iconTop = itemView.top + iconMargin
                    val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    val iconBottom = iconTop + it.intrinsicHeight

                    val scale = alpha.coerceAtLeast(0.5f)
                    val centerX = (iconLeft + iconRight) / 2
                    val centerY = (iconTop + iconBottom) / 2
                    val halfWidth = (it.intrinsicWidth * scale / 2).toInt()
                    val halfHeight = (it.intrinsicHeight * scale / 2).toInt()

                    it.setBounds(
                        centerX - halfWidth,
                        centerY - halfHeight,
                        centerX + halfWidth,
                        centerY + halfHeight
                    )
                    it.alpha = (alpha * 255).toInt()
                    it.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }


            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val item = adapter.getItem(position)
                if (item is OrderItem) {
                    when (item.status) {
                        "Cancelled" -> adapter.showDeleteConfirmation(item.bookingId, position)
                        "Completed" -> adapter.showArchiveConfirmation(item.bookingId, position)
                        "Pending", "Confirmed" -> {
                            adapter.notifyItemChanged(position)
                            SnackBarUtils.showLong(requireContext(),rootView, R.string.toast_error_with_reason, "Бронювання ще активне — спочатку скасуйте")
                        }
                        else -> adapter.notifyItemChanged(position)
                    }
                } else {
                    adapter.notifyItemChanged(position)
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(orderHistoryRecyclerView)

        orderHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        orderHistoryRecyclerView.adapter = adapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> viewModel.loadBookings("created_at", "asc")
                    1 -> viewModel.loadBookings("created_at", "desc")
                    2 -> viewModel.loadBookings("status", "asc")
                    3 -> viewModel.loadBookings("status", "desc")
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        return view
    }

    fun showBookingDetailsBottomSheet(bookingId: Int) {
        val booking = (adapter.getItems().filterIsInstance<OrderItem>()).find { it.bookingId == bookingId }
        if (booking != null) {
            val bottomSheet = BookingDetailsBottomSheet.newInstance(
                bookingId = booking.bookingId,
                hotelName = booking.hotelName,
                roomType = booking.roomType,
                dates = "${booking.checkInDate} - ${booking.checkOutDate}",
                totalPrice = booking.totalPrice,
                roomId = booking.room_id,
                status = booking.status
            )
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        } else {
            SnackBarUtils.showLong(requireContext(),rootView, R.string.toast_error_with_reason, "Бронювання не знайдено")
        }
    }
    private fun deleteBooking(bookingId: Int, position: Int) {
        viewModel.deleteBooking(
            bookingId,
            onSuccess = {
                adapter.removeItem(position)
                SnackBarUtils.showLong(requireContext(),rootView, R.string.toast_error_with_reason, "Бронювання видалено")
            },
            onError = {
                adapter.notifyItemChanged(position)
                SnackBarUtils.showLong(requireContext(),rootView, R.string.toast_error_with_reason, "Помилка при видаленні")
            }
        )
    }

    private fun archiveBooking(bookingId: Int, position: Int) {
        viewModel.archiveBooking(
            bookingId,
            onSuccess = {
                val item = adapter.getItem(position)
                if (item is OrderItem) {
                    item.status = "Archived"
                    adapter.notifyItemChanged(position)
                }
                SnackBarUtils.showLong(requireContext(),rootView, R.string.toast_error_with_reason, "Архівовано")
            },
            onError = {
                adapter.notifyItemChanged(position)
                SnackBarUtils.showLong(requireContext(),rootView, R.string.toast_error_with_reason, "Помилка при архівації")
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
            "completed" -> "Completed"
            else -> "Unknown"
        }
    }
}
