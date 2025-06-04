package com.example.hotelapp.classes.Adapters
import com.bumptech.glide.request.target.Target
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hotelapp.R

class HotelImagesAdapter(private val images: List<String>) :
    RecyclerView.Adapter<HotelImagesAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hotel_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(holder.imageView.context)
            .load(images[position])
            .placeholder(R.drawable.default_image)
            .override(Target.SIZE_ORIGINAL)
            .into(holder.imageView)

    }

    override fun getItemCount(): Int = images.size
}
