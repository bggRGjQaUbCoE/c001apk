package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.IconLinkGridCardBean
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.NetWorkUtil.openLink
import com.google.android.material.imageview.ShapeableImageView

class ImageCarouselCardAdapter(
    private val imageCarouselCardList: List<IconLinkGridCardBean>
) :
    RecyclerView.Adapter<ImageCarouselCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageCarouselCard: ShapeableImageView = view.findViewById(R.id.imageCarouselCard)
        val count: TextView = view.findViewById(R.id.count)
        var title = ""
        var url = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_image_carousel_card_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            openLink(parent.context, viewHolder.url, viewHolder.title.replace("_首页轮播", ""))
        }
        return viewHolder
    }

    override fun getItemCount() = imageCarouselCardList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageCarouselCard = imageCarouselCardList[position]
        holder.title = imageCarouselCard.title
        holder.url = imageCarouselCard.url
        if (itemCount == 1) holder.count.text = "1/1"
        else {
            if (position == 0)
                holder.count.text = "${itemCount - 2}/${itemCount - 2}"
            else if (position == itemCount - 1)
                holder.count.text = "1/${itemCount - 2}"
            else
                holder.count.text = "${position}/${itemCount - 2}"
        }
        ImageUtil.showIMG(holder.imageCarouselCard, imageCarouselCard.pic)
    }

}