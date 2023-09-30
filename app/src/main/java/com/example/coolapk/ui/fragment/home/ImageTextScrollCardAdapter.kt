package com.example.coolapk.ui.fragment.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coolapk.R
import com.example.coolapk.logic.model.HomeFeedResponse
import com.example.coolapk.util.DensityTool
import com.example.coolapk.util.ImageShowUtil

class ImageTextScrollCardAdapter(
    private val mContext: Context,
    private val imageCarouselCardList: List<HomeFeedResponse.Entities>
) :
    RecyclerView.Adapter<ImageTextScrollCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val imageTextScrollCard: ImageView = view.findViewById(R.id.imageTextScrollCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_image_text_scroll_card_item, parent, false)
        val padding = 50f
        val spacePx = (DensityTool.dp2px(parent.context, padding))
        val imageWidth = DensityTool.getScreenWidth(parent.context) - spacePx
        view.layoutParams.width = (imageWidth - imageWidth / 3).toInt()
        return ViewHolder(view)
    }

    override fun getItemCount() = imageCarouselCardList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageTextScrollCard = imageCarouselCardList[position]
        holder.title.text = imageTextScrollCard.title
        val space = mContext.resources.getDimensionPixelSize(R.dimen.normal_space)
        holder.title.setPadding(space, space, space, space)
        ImageShowUtil.showIMG1(holder.imageTextScrollCard, imageTextScrollCard.pic)
    }

    private fun http2https(url: String) =
        if (StringBuilder(url)[4] != 's')
            StringBuilder(url).insert(4, "s").toString()
        else url


}