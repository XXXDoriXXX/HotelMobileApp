package com.example.hotelapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemsRoomAdapter(var items:List<RoomItem>, var context: Context) : RecyclerView.Adapter<ItemsRoomAdapter.MyViewHolder>(){

    class MyViewHolder(view: View):RecyclerView.ViewHolder(view){
        val image: ImageView =view.findViewById(R.id.room_image)
        val roomnumber: TextView =view.findViewById(R.id.room_number)
        val places: TextView =view.findViewById(R.id.room_places)
        val type: TextView =view.findViewById(R.id.room_type)
        val price: TextView =view.findViewById(R.id.room_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): com.example.hotelapp.ItemsRoomAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_item,parent,false)
        return com.example.hotelapp.ItemsRoomAdapter.MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: com.example.hotelapp.ItemsRoomAdapter.MyViewHolder, position: Int) {
        try {


            var imageid = context.resources.getIdentifier(
                items[position].image,
                "drawable",
                context.packageName
            )

            if (imageid != 0) {
                holder.image.setImageResource(imageid)
            } else {
                holder.image.setImageResource(R.drawable.default_image)
            }
            holder.roomnumber.text ="Room number: "+items[position].number.toString()
            holder.type.text = "Status: "+items[position].type
            holder.places.text ="Places: "+ items[position].places.toString()
            holder.price.text = "$"+items[position].price.toString()
            holder.image.setOnClickListener {
                HotelHolder.currentRoom = items[position]
                val intent = Intent(context,Current_Room_Info::class.java)
                context.startActivity(intent)

            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

}