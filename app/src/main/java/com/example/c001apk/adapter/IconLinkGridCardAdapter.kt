package com.example.c001apk.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.CarouselActivity
import com.example.c001apk.ui.activity.TopicActivity
import com.example.c001apk.util.ImageShowUtil

class IconLinkGridCardAdapter(
    private val iconLinkGridCardList: List<HomeFeedResponse.Entities>
) :
    RecyclerView.Adapter<IconLinkGridCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        var url = ""
        val iconLinkGridCard: ImageView = view.findViewById(R.id.iconLinkGridCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_icon_link_grid_card_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            if (viewHolder.url.contains("/t/")) {
                val intent = Intent(parent.context, TopicActivity::class.java)
                intent.putExtra("type", "topic")
                intent.putExtra("title", viewHolder.title.text)
                intent.putExtra("url", viewHolder.url)
                intent.putExtra("id", "")
                parent.context.startActivity(intent)
            } else if (viewHolder.url.contains("/page?url=")) {
                val intent = Intent(parent.context, CarouselActivity::class.java)
                intent.putExtra("title", viewHolder.title.text)
                val url = viewHolder.url.replace("/page?url=", "")
                intent.putExtra("url", url)
                parent.context.startActivity(intent)
            } else if (viewHolder.url.contains("#/feed/")) {
                val intent = Intent(parent.context, CarouselActivity::class.java)
                intent.putExtra("title", viewHolder.title.text)
                val url = viewHolder.url
                intent.putExtra("url", url)
                parent.context.startActivity(intent)
            }

        }
        return viewHolder
    }

    override fun getItemCount() = iconLinkGridCardList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val iconLinkGridCard = iconLinkGridCardList[position]
        holder.title.text = iconLinkGridCard.title
        holder.url = iconLinkGridCard.url
        ImageShowUtil.showIMG(holder.iconLinkGridCard, iconLinkGridCard.pic)
    }

}