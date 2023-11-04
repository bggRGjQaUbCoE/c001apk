package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.CarouselActivity
import com.example.c001apk.ui.activity.FeedActivity
import com.example.c001apk.ui.activity.TopicActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.util.ImageShowUtil
import com.google.android.material.imageview.ShapeableImageView

class ImageCarouselCardAdapter(
    private val imageCarouselCardList: List<HomeFeedResponse.Entities>
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
            if (viewHolder.url.contains("/feed/")) {
                val intent = Intent(parent.context, FeedActivity::class.java)
                intent.putExtra("type", "feed")
                intent.putExtra("id", viewHolder.url.replace("/feed/", ""))
                intent.putExtra("uid", "")
                intent.putExtra("uname", "")
                parent.context.startActivity(intent)
            } else if (viewHolder.url.contains("/t/")) {
                val intent = Intent(parent.context, TopicActivity::class.java)
                intent.putExtra("type", "topic")
                intent.putExtra("title", viewHolder.title)
                intent.putExtra("url", viewHolder.url)
                intent.putExtra("id", "")
                parent.context.startActivity(intent)
            } else if (viewHolder.url.contains("/page?url")) {
                val intent = Intent(parent.context, CarouselActivity::class.java)
                intent.putExtra("url", viewHolder.url.substring(10, viewHolder.url.length))
                intent.putExtra("title", viewHolder.title)
                parent.context.startActivity(intent)
            } else if (viewHolder.url.contains("https://") || viewHolder.url.contains("http://")) {
                val intent = Intent(parent.context, WebViewActivity::class.java)
                intent.putExtra("url", viewHolder.url)
                parent.context.startActivity(intent)
            }
        }
        return viewHolder
    }

    override fun getItemCount() = imageCarouselCardList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageCarouselCard = imageCarouselCardList[position]
        holder.title = imageCarouselCard.title
        holder.url = imageCarouselCard.url
        holder.count.text = "${position + 1}/${imageCarouselCardList.size}"
        ImageShowUtil.showIMG(holder.imageCarouselCard, imageCarouselCard.pic)
    }

}