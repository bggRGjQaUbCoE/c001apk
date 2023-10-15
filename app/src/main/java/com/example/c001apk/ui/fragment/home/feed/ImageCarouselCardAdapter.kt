package com.example.c001apk.ui.fragment.home.feed

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.util.ImageShowUtil

class ImageCarouselCardAdapter(
    private val imageCarouselCardList: List<HomeFeedResponse.Entities>
) :
    RecyclerView.Adapter<ImageCarouselCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageCarouselCard: ImageView = view.findViewById(R.id.imageCarouselCard)
        val count: TextView = view.findViewById(R.id.count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_image_carousel_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = imageCarouselCardList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageCarouselCard = imageCarouselCardList[position]
        ImageShowUtil.showIMG(holder.imageCarouselCard, imageCarouselCard.pic)
        holder.count.text = "${position + 1}/${imageCarouselCardList.size}"
    }

}