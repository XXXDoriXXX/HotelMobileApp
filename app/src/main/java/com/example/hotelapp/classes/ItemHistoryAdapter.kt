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
class ItemHistoryAdapter(
    private var items: MutableList<OrderItem>,
    private val context: Context
) : RecyclerView.Adapter<ItemHistoryAdapter.MyViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addOrder(order: OrderItem) {
        items.add(order)
        notifyItemInserted(items.size - 1)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        try {
            holder.bind(items[position])

            holder.itemView.setOnClickListener {
                val order = items[position]

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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
