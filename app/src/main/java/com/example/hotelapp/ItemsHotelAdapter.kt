package com.example.hotelapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import HotelItem
import com.bumptech.glide.Glide
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.Holder.apiHolder

class ItemsHotelAdapter(var items:List<HotelItem>, var context: Context) : RecyclerView.Adapter<ItemsHotelAdapter.MyViewHoldert>(){

    class   MyViewHoldert(view: View): RecyclerView.ViewHolder(view){
        val image: ImageView =view.findViewById(R.id.hotel_image)
        val hotelname: TextView =view.findViewById(R.id.hotel_name)
        val title: TextView =view.findViewById(R.id.hotel_description)
        val description: TextView =view.findViewById(R.id.hotel_description)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHoldert {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_item,parent,false)
        return MyViewHoldert(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: MyViewHoldert, position: Int) {
        val currentItem = items[position]

        holder.title.text = currentItem.name
        holder.hotelname.text = currentItem.name
        holder.description.text = "Address: ${currentItem.address}"

        val imageUrl = currentItem.images?.firstOrNull()?.image_url
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(apiHolder.BASE_URL+imageUrl)
                .placeholder(R.drawable.default_image)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.default_image)
        }

        holder.image.setOnClickListener {
            HotelHolder.currentHotel = currentItem
            val intent = Intent(context, CurrentHotelInfo::class.java)
            context.startActivity(intent)
        }
    }



}