package com.example.hotelapp.classes

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hotelapp.Current_Room_Info
import com.example.hotelapp.Holder.HotelHolder
import com.example.hotelapp.Holder.apiHolder
import com.example.hotelapp.R

class ItemsRoomAdapter(var items:List<RoomItem>, var context: Context) : RecyclerView.Adapter<ItemsRoomAdapter.MyViewHolder>(){

    class MyViewHolder(view: View):RecyclerView.ViewHolder(view){
        val image: ImageView =view.findViewById(R.id.room_image)
        val roomnumber: TextView =view.findViewById(R.id.room_number)
        val places: TextView =view.findViewById(R.id.room_places)
        val type: TextView =view.findViewById(R.id.room_type)
        val price: TextView =view.findViewById(R.id.room_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_item,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        try {

            val imageUrl = items[position].images?.firstOrNull()?.image_url
            if (!imageUrl.isNullOrEmpty()) {
                Log.d("RoomImageURL", "Loading image: $imageUrl")
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_image)
                    .into(holder.image)
            } else {
                holder.image.setImageResource(R.drawable.default_image)
            }
            holder.roomnumber.text ="Room number: "+items[position].room_number.toString()
            holder.type.text = "Status: "+items[position].room_type
            holder.places.text ="Places: "+ items[position].places.toString()
            holder.price.text = "$"+items[position].price_per_night.toString()
            holder.image.setOnClickListener {
                HotelHolder.currentRoom = items[position]
                val intent = Intent(context, Current_Room_Info::class.java)
                context.startActivity(intent)

            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

}