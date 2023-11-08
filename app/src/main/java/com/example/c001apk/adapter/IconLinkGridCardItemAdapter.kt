package com.example.c001apk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.logic.model.IconLinkGridCardBean
import com.example.c001apk.ui.activity.CarouselActivity
import com.example.c001apk.ui.activity.TopicActivity
import com.example.c001apk.util.ImageShowUtil

class IconLinkGridCardItemAdapter(
    private val mContext: Context,
    private val dataList: List<IconLinkGridCardBean>
) : RecyclerView.Adapter<IconLinkGridCardItemAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var iconLinkGridCard: ImageView = itemView.findViewById(R.id.iconLinkGridCard)
        var title: TextView = itemView.findViewById(R.id.title)
        var url = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(mContext)
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.title.text = data.title
        holder.url = data.url
        ImageShowUtil.showIMG(holder.iconLinkGridCard, data.pic)
    }

    override fun getItemCount() = dataList.size
}