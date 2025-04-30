package com.example.hotelapp.classes

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import HotelItem
import android.util.Log
import android.widget.RatingBar
import com.bumptech.glide.Glide
import com.example.hotelapp.ui.CurrentHotelInfo
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.Holder.apiHolder
import com.example.hotelapp.R

class ItemsHotelAdapter(
    var items: List<HotelItem>,
    var context: Context,
    private var isVerticalLayout: Boolean
) : RecyclerView.Adapter<ItemsHotelAdapter.MyViewHoldert>() {

    class MyViewHoldert(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.hotel_image)
        val hotelName: TextView = view.findViewById(R.id.hotel_name)
        val description: TextView = view.findViewById(R.id.hotel_description)
        val rating: RatingBar = view.findViewById(R.id.rating_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHoldert {
        val layoutId = if (isVerticalLayout) R.layout.hotel_item_2 else R.layout.hotel_item
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MyViewHoldert(view)
    }

    override fun getItemCount(): Int = items.count()

    override fun onBindViewHolder(holder: MyViewHoldert, position: Int) {
        val currentItem = items[position]
        holder.hotelName.text = currentItem.name
        holder.description.text = currentItem.address.country+" "+currentItem.address.city+ "\n"+currentItem.description
        holder.rating.rating = currentItem.rating

        val imageUrl = currentItem.images?.firstOrNull()?.image_url
        Log.d("ItemsHotelAdapter", "Loading image: ${apiHolder.BASE_URL + imageUrl}")

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.image.context)
                .load(imageUrl)
                .placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.default_image)
        }

        holder.image.setOnClickListener {
            HotelHolder.currentHotel = currentItem
            val intent = Intent(context, CurrentHotelInfo::class.java).apply {
                putStringArrayListExtra("HOTEL_IMAGES", ArrayList(currentItem.images?.map { it.image_url } ?: listOf()))
            }
            context.startActivity(intent)
        }
    }

    fun toggleLayout(isVertical: Boolean) {
        isVerticalLayout = isVertical
        notifyDataSetChanged()
    }
}
