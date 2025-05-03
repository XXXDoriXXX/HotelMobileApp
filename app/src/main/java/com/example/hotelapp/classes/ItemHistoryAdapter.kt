package com.example.hotelapp.classes

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hotelapp.ui.BookingDetailsActivity
import com.example.hotelapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ItemHistoryAdapter(
    private var items: MutableList<Any>,
    private val context: Context,
    private val onDeleteBooking: (bookingId: Int, position: Int) -> Unit,
    private val onArchiveBooking: (bookingId: Int, position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val hotelName: TextView = itemView.findViewById(R.id.order_hotel_name)
        private val roomType: TextView = itemView.findViewById(R.id.order_room_type)
        private val orderDates: TextView = itemView.findViewById(R.id.order_dates)
        private val totalPrice: TextView = itemView.findViewById(R.id.order_total_price)
        private val statusText: TextView = itemView.findViewById(R.id.order_status)
        private val hotelImage: ImageView = itemView.findViewById(R.id.order_hotel_image)

        fun bind(order: OrderItem) {
            hotelName.text = order.hotelName
            roomType.text = "Room: ${order.roomType}"
            orderDates.text = "${order.checkInDate} - ${order.checkOutDate}"
            totalPrice.text = "Total: $${order.totalPrice}"
            statusText.text = order.status

            when (order.status) {
                "Pending" -> statusText.setBackgroundResource(R.drawable.status_label_background_yellow)
                "Cancelled" -> statusText.setBackgroundResource(R.drawable.status_label_background_red)
                    "Confirmed" -> statusText.setBackgroundResource(R.drawable.status_label_background_green)
                "Completed" -> statusText.setBackgroundResource(R.drawable.status_label_background_gray)
                else -> statusText.setBackgroundResource(R.drawable.status_label_background)
            }

            val fullImageUrl = order.hotel_image_url
            Glide.with(itemView.context)
                .load(fullImageUrl)
                .placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(hotelImage)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_header, parent, false)
            DateViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
            MyViewHolder(view)
        }
    }


    override fun getItemCount(): Int {
        return items.size
    }

    fun addOrder(order: OrderItem) {
        items.add(order)
        notifyItemInserted(items.size - 1)
    }
    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) 0 else 1
    }
    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateText: TextView = view.findViewById(R.id.date_text)
        fun bind(date: String) {
            dateText.text = date
        }
    }
    fun getItem(position: Int): Any = items[position]

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun showDeleteConfirmation(bookingId: Int, position: Int) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Видалити бронювання?")
            .setMessage("Це дія є незворотною. Ви впевнені?")
            .setPositiveButton("Так") { _, _ -> onDeleteBooking(bookingId, position) }
            .setNegativeButton("Ні") { dialog, _ -> dialog.dismiss(); notifyItemChanged(position) }
            .show()
    }

    fun showArchiveConfirmation(bookingId: Int, position: Int) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Архівувати бронювання?")
            .setMessage("Бронювання буде переміщене в архів.")
            .setPositiveButton("Так") { _, _ -> onArchiveBooking(bookingId, position) }
            .setNegativeButton("Ні") { dialog, _ -> dialog.dismiss(); notifyItemChanged(position) }
            .show()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MyViewHolder -> {
                val order = items[position] as OrderItem
                holder.bind(order)

                holder.itemView.setOnClickListener {
                    if (context is androidx.fragment.app.FragmentActivity) {
                        val bottomSheet = com.example.hotelapp.ui.BookingDetailsBottomSheet.newInstance(
                            bookingId = order.bookingId,
                            hotelName = order.hotelName,
                            roomType = order.roomType,
                            dates = "${order.checkInDate} - ${order.checkOutDate}",
                            totalPrice = order.totalPrice,
                            roomId = order.room_id,
                            status = order.status
                        )
                        bottomSheet.show((context as androidx.fragment.app.FragmentActivity).supportFragmentManager, bottomSheet.tag)
                    }
                }
            }

            is DateViewHolder -> {
                val date = items[position] as String
                holder.bind(date)
            }
        }
    }


}
