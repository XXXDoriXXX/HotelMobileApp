package com.example.hotelapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemsHotelAdapter(var items:List<HotelItem>, var context: Context) : RecyclerView.Adapter<ItemsHotelAdapter.MyViewHoldert>(){

    class   MyViewHoldert(view: View): RecyclerView.ViewHolder(view){
        val image: ImageView =view.findViewById(R.id.hotel_image)
        val hotelname: TextView =view.findViewById(R.id.hotel_name)
        val title: TextView =view.findViewById(R.id.hotel_description)
        //val description: TextView =view.findViewById(R.id.item_hotel_list_text)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHoldert {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_item,parent,false)
        return MyViewHoldert(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: MyViewHoldert, position: Int) {
        holder.title.text = items[position].title
        holder.hotelname.text = items[position].name
      //  holder.description.text = items[position].desc

        var imageid = context.resources.getIdentifier(
            items[position].image,
            "drawable",
            context.packageName
        )

        if (imageid != 0) {
            holder.image.setImageResource(imageid)
        } else {
            holder.image.setImageResource(R.drawable.default_hotel_image)
        }

        holder.image.setOnClickListener {
            HotelHolder.currentHotel = items[position]
            val intent = Intent(context,CurrentHotelInfo::class.java)
            context.startActivity(intent)
        }
    }

}