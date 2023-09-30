package com.example.coolapk.ui.fragment.home.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.coolapk.R
import com.example.coolapk.logic.model.HomeFeedResponse
import com.example.coolapk.util.ImageShowUtil

class ImageCarouselCardAdapter(
    private val imageCarouselCardList: List<HomeFeedResponse.Entities>
) :
    RecyclerView.Adapter<ImageCarouselCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageCarouselCard: ImageView = view.findViewById(R.id.imageCarouselCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_image_carousel_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = imageCarouselCardList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageCarouselCard = imageCarouselCardList[position]
        ImageShowUtil.showIMG(holder.imageCarouselCard, imageCarouselCard.pic)
    }

}