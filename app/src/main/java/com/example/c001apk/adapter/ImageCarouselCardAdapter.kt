package com.example.c001apk.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.IconLinkGridCardBean
import com.example.c001apk.ui.activity.AppActivity
import com.example.c001apk.ui.activity.CarouselActivity
import com.example.c001apk.ui.activity.FeedActivity
import com.example.c001apk.ui.activity.TopicActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.util.ImageUtil
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
            if (viewHolder.url.startsWith("/feed/")) {
                val intent = Intent(parent.context, FeedActivity::class.java)
                intent.putExtra("type", "feed")
                intent.putExtra("id", viewHolder.url.replace("/feed/", ""))
                intent.putExtra("uid", "")
                intent.putExtra("uname", "")
                parent.context.startActivity(intent)
            } else if (viewHolder.url.startsWith("/t/")) {
                val intent = Intent(parent.context, TopicActivity::class.java)
                intent.putExtra("type", "topic")
                intent.putExtra("title", viewHolder.title)
                intent.putExtra("url", viewHolder.url)
                intent.putExtra("id", "")
                parent.context.startActivity(intent)
            } else if (viewHolder.url.startsWith("/product/")) {
                val intent = Intent(parent.context, TopicActivity::class.java)
                intent.putExtra("type", "product")
                intent.putExtra("title", viewHolder.title.replace("_首页轮播", ""))
                intent.putExtra("url", viewHolder.url)
                intent.putExtra("id", viewHolder.url.replace("/product/", ""))
                parent.context.startActivity(intent)
            } else if (viewHolder.url.startsWith("/page?url")) {
                val intent = Intent(parent.context, CarouselActivity::class.java)
                intent.putExtra("url", viewHolder.url.replace("/page?url", ""))
                intent.putExtra("title", viewHolder.title)
                parent.context.startActivity(intent)
            } else if (viewHolder.url.startsWith("/game/")) {
                val intent = Intent(parent.context, AppActivity::class.java)
                intent.putExtra("id", viewHolder.url.replace("/game/", ""))
                parent.context.startActivity(intent)
            } else if (viewHolder.url.startsWith("/apk/")) {
                val intent = Intent(parent.context, AppActivity::class.java)
                intent.putExtra("id", viewHolder.url.replace("/apk/", ""))
                parent.context.startActivity(intent)
            } else if (viewHolder.url.startsWith("https://") || viewHolder.url.contains("http://")) {
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