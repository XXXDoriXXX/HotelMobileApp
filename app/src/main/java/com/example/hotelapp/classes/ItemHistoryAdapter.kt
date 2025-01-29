package com.example.hotelapp.classes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.OrderItem
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

        fun bind(order: OrderItem) {
            hotelName.text = order.hotelName
            roomType.text = "Room: ${order.roomType}"
            orderDates.text = "${order.checkInDate} - ${order.checkOutDate}"
            totalPrice.text = "Total: $${order.totalPrice}"
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
        notifyItemInserted(items.size - 1) // Сповіщаємо про додавання нового елемента
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        try {
            holder.bind(items[position])
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
